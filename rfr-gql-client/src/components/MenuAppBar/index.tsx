import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import {Avatar, Tooltip} from '@mui/material';
import {useColorScheme} from '@mui/material/styles';
import MenuIcon from '@mui/icons-material/Menu';
import ExitToAppOutlinedIcon from '@mui/icons-material/ExitToAppOutlined';
import DarkModeOutlinedIcon from '@mui/icons-material/DarkModeOutlined';
import LightModeOutlinedIcon from '@mui/icons-material/LightModeOutlined';
import {Sidebar} from '../Sidebar';
import {FC, useContext} from 'react';
import {apiClient} from '../../api/apolloClient';
import {
    accessTokenFromLocalStorage,
    clearSession,
    getLogoutLink,
    getRevokeTokenParams,
    idTokenFromLocalStorage
} from '../../api/authUtils';
import {authClient} from '../../api/authClient';
import {Link} from 'react-router';
import {SessionContext} from '../../context/SessionContext';
import './styles.css';

interface MenuAppBarInterface {
    sidebarState: boolean,
    handleChangeState: (isOpened: boolean) => void,
}

export const MenuAppBar: FC<MenuAppBarInterface> = ({sidebarState, handleChangeState}) => {
    const {user} = useContext(SessionContext);
    const {mode, setMode} = useColorScheme();
    const isDark = mode === "dark";

    const onLogoutClick = async () => {
        const accessToken = accessTokenFromLocalStorage();
        const idToken = idTokenFromLocalStorage();
        if (accessToken) {
            try {
                await authClient.revokeToken(getRevokeTokenParams(accessToken));
            } catch (e) {
                console.error("[Logout] access token was not revoked:", e);
            }
        }
        await apiClient.clearStore();
        if (!idToken) {
            clearSession();
            window.location.replace("/");
            return;
        }
        window.location.replace(getLogoutLink(idToken));
    }

    return (
        <Box sx={{
            flexGrow: 1,
            marginBottom: 2,
            display: "flex",
        }}>
            <AppBar
                position="fixed"
                color="inherit"
                sx={{
                    zIndex: (theme) => theme.zIndex.drawer + 1,
                    bgcolor: "background.paper",
                    borderBottom: 1,
                    borderColor: "divider",
                }}
            >
                <Toolbar>
                    <IconButton
                        size="large"
                        edge="start"
                        aria-label="open drawer"
                        color="inherit"
                        sx={{
                            marginRight: 3,
                        }}
                        onClick={() => handleChangeState(!sidebarState)}
                        component="button"
                    >
                        <MenuIcon/>
                    </IconButton>
                    <Link to={"/my-travels"} className="link">
                        <Typography variant="h5" component="h1" sx={{fontWeight: 700, color: "text.primary"}}>
                            <Box component="span" sx={{color: "primary.main"}}>R</Box>angiffler
                        </Typography>
                    </Link>
                    <Box sx={{
                        marginLeft: "auto",
                        display: "flex",
                        alignItems: "center",
                        gap: 1,
                    }}
                    >
                        <Tooltip title={isDark ? "Light mode" : "Dark mode"}>
                            <IconButton
                                aria-label="Toggle dark mode"
                                color="inherit"
                                onClick={() => setMode(isDark ? "light" : "dark")}
                            >
                                {isDark ? <LightModeOutlinedIcon/> : <DarkModeOutlinedIcon/>}
                            </IconButton>
                        </Tooltip>
                        <Tooltip title="Profile">
                            <IconButton component={Link} to="/profile" aria-label="Profile" sx={{p: 0.5}}>
                                <Avatar
                                    src={user?.avatar || undefined}
                                    alt={user?.username}
                                    sx={{width: 34, height: 34, bgcolor: "primary.main", color: "primary.contrastText"}}
                                >
                                    {user?.username?.charAt(0).toUpperCase()}
                                </Avatar>
                            </IconButton>
                        </Tooltip>
                        <Tooltip title="Logout">
                            <IconButton
                                aria-label="Logout"
                                onClick={onLogoutClick}
                                color="inherit"
                            >
                                <ExitToAppOutlinedIcon/>
                            </IconButton>
                        </Tooltip>
                    </Box>
                </Toolbar>
            </AppBar>
            <Sidebar sidebarState={sidebarState}/>
        </Box>
    );
}
