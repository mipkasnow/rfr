import {Fab, useScrollTrigger} from "@mui/material";
import AddAPhotoOutlinedIcon from '@mui/icons-material/AddAPhotoOutlined';
import {FC} from "react";

interface AddPhotoFabInterface {
    onClick: () => void;
}

export const AddPhotoFab: FC<AddPhotoFabInterface> = ({onClick}) => {
    const collapsed = useScrollTrigger({threshold: 120});

    return (
        <Fab
            color="primary"
            variant={collapsed ? "circular" : "extended"}
            aria-label="Add photo"
            onClick={onClick}
            sx={{
                position: "fixed",
                right: {xs: 16, md: 32},
                bottom: {xs: 16, md: 32},
                zIndex: (theme) => theme.zIndex.speedDial,
            }}
        >
            <AddAPhotoOutlinedIcon sx={{mr: collapsed ? 0 : 1}}/>
            {!collapsed && "Add photo"}
        </Fab>
    );
};
