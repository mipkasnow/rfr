import {Avatar, Box, Chip, Container, Typography} from "@mui/material";
import CancelRoundedIcon from '@mui/icons-material/CancelRounded';
import {PhotoContainer} from "../../components/PhotoContainer";
import {WorldMap} from "../../components/WorldMap";
import {Toggle} from "../../components/Toggle";
import {useRef, useState} from "react";
import {useGetFeed} from "../../hooks/useGetFeed";
import {useDialog} from "../../context/useDialog.ts";
import {useCountries} from "../../context/useCountries";
import {formInitialState} from "../../components/PhotoModal/formValidate.ts";
import {QueryErrorAlert} from "../../components/QueryErrorAlert";
import {AddPhotoFab} from "../../components/AddPhotoFab";
import {VisitedCountries} from "../../components/VisitedCountries";
import {CollapsibleMapCard} from "../../components/CollapsibleMapCard";

export const MyTravelsPage = () => {
    const [withFriends, setWithFriends] = useState(false);
    const [countryFilter, setCountryFilter] = useState<string | null>(null);
    const {photos, stat, hasNextPage, initialLoading, refreshing, loadingMore, loadMore, error, refetch} =
        useGetFeed({withFriends, country: countryFilter});
    const {countries} = useCountries();
    const filterCountry = countries?.find((c) => c.code === countryFilter);
    const filterCountryName = filterCountry?.name ?? countryFilter?.toUpperCase();

    const dialog = useDialog();

    const handleAddClick = () => {
        dialog.showDialog({
            title: "Add photo",
            isEdit: false,
            withFriends,
            formData: {...formInitialState,},
        });
    };

    const photosHeaderRef = useRef<HTMLDivElement>(null);
    const toggleCountryFilter = (code: string) => setCountryFilter((current) => current === code ? null : code);
    const selectCountryFromList = (code: string) => {
        setCountryFilter(code);
        photosHeaderRef.current?.scrollIntoView({behavior: "smooth", block: "start"});
    };
    const resetCountryFilter = () => setCountryFilter(null);

    return (
        <Container maxWidth={false} sx={{pb: 12, maxWidth: 2400}}>
            <Box sx={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                flexWrap: "wrap",
                gap: 2,
                mb: 3,
            }}>
                <Box>
                    <Typography variant="h4" component="h2">
                        Travels map
                    </Typography>
                    <VisitedCountries stat={stat} selectedCountry={countryFilter} onSelect={selectCountryFromList}/>
                </Box>
                <Toggle withMyFriends={withFriends} setWithMyFriends={setWithFriends}/>
            </Box>
            <CollapsibleMapCard>
                <WorldMap data={stat} selectedCountry={countryFilter} onCountryClick={toggleCountryFilter}/>
            </CollapsibleMapCard>
            <Box ref={photosHeaderRef} sx={{display: "flex", alignItems: "center", flexWrap: "wrap", gap: 1.5, minHeight: 40, mb: 2, scrollMarginTop: 88}}>
                <Typography variant="h5" component="h3" sx={{fontWeight: 700}}>
                    Photos
                </Typography>
                {countryFilter && (
                    <Chip
                        avatar={<Avatar src={filterCountry?.flag} alt=""/>}
                        label={filterCountryName}
                        onDelete={resetCountryFilter}
                        color="primary"
                        variant="outlined"
                        aria-label={`Country filter: ${filterCountryName}`}
                        deleteIcon={<CancelRoundedIcon role="button" aria-hidden={false} aria-label="Reset country filter"/>}
                    />
                )}
            </Box>
            <QueryErrorAlert error={error} fallback="Can not load travels" onRetry={() => refetch()}/>
            <PhotoContainer
                withFriends={withFriends}
                loading={initialLoading}
                loadingMore={loadingMore}
                refreshing={refreshing}
                data={photos}
                hasNextPage={hasNextPage}
                loadMore={loadMore}
                onAddClick={handleAddClick}
                filterCountryName={countryFilter ? filterCountryName : undefined}
                onResetFilter={resetCountryFilter}
            />
            <AddPhotoFab onClick={handleAddClick}/>
        </Container>
    )
}
