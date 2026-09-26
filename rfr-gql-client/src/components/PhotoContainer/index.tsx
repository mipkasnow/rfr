import Grid from "@mui/material/Grid";
import {FC, useEffect, useRef} from "react";
import {PhotoCard} from "../PhotoCard";
import {Photo} from "../../types/Photo";
import {Box, Button, Card, CircularProgress, Skeleton, Typography} from "@mui/material";
import AddAPhotoOutlinedIcon from '@mui/icons-material/AddAPhotoOutlined';
import TravelExploreRoundedIcon from '@mui/icons-material/TravelExploreRounded';
import {useDialog} from "../../context/useDialog.ts";
import {formInitialState} from "../PhotoModal/formValidate.ts";

const gridItemSize = {xs: 12, sm: 6, md: 4, lg: 3, xl: 2};
const SKELETON_COUNT = 8;

interface PhotoContainerInterface {
    data: Photo[];
    hasNextPage: boolean;
    loadMore: () => void;
    loading: boolean;
    loadingMore: boolean;
    refreshing?: boolean;
    withFriends: boolean;
    onAddClick: () => void;
    filterCountryName?: string;
    onResetFilter?: () => void;
}

export const PhotoContainer: FC<PhotoContainerInterface> = ({
                                                                data,
                                                                hasNextPage,
                                                                loadMore,
                                                                loading,
                                                                loadingMore,
                                                                refreshing = false,
                                                                withFriends,
                                                                onAddClick,
                                                                filterCountryName,
                                                                onResetFilter,
                                                            }) => {
    const dialog = useDialog();
    const sentinelRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const sentinel = sentinelRef.current;
        if (!sentinel || !hasNextPage) {
            return;
        }
        const observer = new IntersectionObserver(
            (entries) => entries[0].isIntersecting && loadMore(),
            {rootMargin: "400px"},
        );
        observer.observe(sentinel);
        return () => observer.disconnect();
    }, [hasNextPage, loadMore]);

    const handleSelectImage = (image: Photo) => {
        dialog.showDialog({
            title: "Edit photo",
            isEdit: true,
            withFriends,
            formData: {
                ...formInitialState,
                id: image.id,
                description: {
                    ...formInitialState.description,
                    value: image.description
                },
                country: {
                    ...formInitialState.country,
                    value: image.country.code,
                },
                src: {
                    ...formInitialState.src,
                    value: image.src,
                }
            },
        });
    };

    if (loading) {
        return (
            <Grid container spacing={3}>
                {Array.from({length: SKELETON_COUNT}, (_, i) => (
                    <Grid key={i} size={gridItemSize}>
                        <Card>
                            <Skeleton variant="rectangular" sx={{aspectRatio: "4 / 3", height: "auto"}}/>
                            <Box sx={{p: 2}}>
                                <Skeleton width="80%"/>
                                <Skeleton width="40%"/>
                            </Box>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        );
    }

    if (!data.length && filterCountryName) {
        return (
            <Card sx={{py: 8, px: 3, textAlign: "center"}}>
                <TravelExploreRoundedIcon sx={{fontSize: 72, color: "primary.main", opacity: 0.6}}/>
                <Typography variant="h6" component="p" sx={{mt: 2}}>
                    No photos from {filterCountryName}
                </Typography>
                <Button variant="outlined" onClick={onResetFilter} sx={{mt: 3}}>
                    Show all countries
                </Button>
            </Card>
        );
    }

    if (!data.length) {
        return (
            <Card sx={{py: 8, px: 3, textAlign: "center"}}>
                <TravelExploreRoundedIcon sx={{fontSize: 72, color: "primary.main", opacity: 0.6}}/>
                <Typography variant="h6" component="p" sx={{mt: 2}}>
                    No travels yet
                </Typography>
                <Typography variant="body2" sx={{color: "text.secondary", mt: 0.5, mb: 3}}>
                    Share a photo from your trip — the country will light up on the map.
                </Typography>
                <Button variant="contained" startIcon={<AddAPhotoOutlinedIcon/>} onClick={onAddClick}>
                    Add your first photo
                </Button>
            </Card>
        );
    }

    return (
        <>
            <Grid
                container
                spacing={3}
                aria-busy={refreshing}
                sx={{opacity: refreshing ? 0.55 : 1, transition: "opacity 0.2s ease"}}
            >
                {data.map((item: Photo) => (
                    <Grid key={item.id} size={gridItemSize}>
                        <PhotoCard
                            photo={item}
                            onEditClick={() => handleSelectImage(item)}
                        />
                    </Grid>
                ))}
            </Grid>
            <Box ref={sentinelRef} sx={{display: "flex", justifyContent: "center", py: 4}}>
                {loadingMore && <CircularProgress size={28}/>}
            </Box>
        </>
    );
};
