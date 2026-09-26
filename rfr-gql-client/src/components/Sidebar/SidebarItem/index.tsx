import {ListItem, ListItemButton, ListItemIcon, ListItemText, Tooltip} from "@mui/material"
import {FC, ReactNode} from "react";
import {Link, useLocation} from 'react-router';

interface SidebarItemProps {
    name: string,
    link: string,
    icon: ReactNode,
    sidebarState: boolean,
}

export const SidebarItem: FC<SidebarItemProps> = ({name, link, icon, sidebarState}) => {
    const {pathname} = useLocation();
    const selected = pathname === link;

    return (
        <ListItem disablePadding sx={{display: 'block', mb: 0.5}}>
            <Tooltip title={sidebarState ? "" : name} placement="right">
                <ListItemButton
                    component={Link}
                    to={link}
                    selected={selected}
                    sx={{
                        minHeight: 48,
                        borderRadius: 2,
                        justifyContent: sidebarState ? 'initial' : 'center',
                        px: 1.75,
                        color: selected ? "primary.main" : "text.secondary",
                        "&.Mui-selected": {
                            bgcolor: "action.selected",
                        },
                    }}
                >
                    <ListItemIcon
                        sx={{
                            minWidth: 0,
                            mr: sidebarState ? 2 : 'auto',
                            justifyContent: 'center',
                            color: "inherit",
                        }}
                    >
                        {icon}
                    </ListItemIcon>
                    <ListItemText
                        primary={name}
                        sx={{opacity: sidebarState ? 1 : 0}}
                        slotProps={{primary: {sx: {fontWeight: 600}}}}
                    />
                </ListItemButton>
            </Tooltip>
        </ListItem>
    )
}
