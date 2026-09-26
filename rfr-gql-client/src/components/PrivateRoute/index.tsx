import {Navigate, Outlet} from "react-router"
import {MenuAppBar} from "../MenuAppBar"
import {Box} from "@mui/material"
import {useState} from "react";
import {DrawerHeader} from "../Drawer/DrawerHeader";
import {drawerWidth} from "../Drawer";
import {SessionContext} from "../../context/SessionContext";
import {useGetUser} from "../../hooks/useGetUser";
import {Loader} from "../Loader";
import {CountriesProvider} from "../../context/CountriesContext";
import {DialogProvider} from "../../context/DialogContext.tsx";
import {QueryErrorAlert} from "../QueryErrorAlert";
import {isUnauthorized} from "../../api/graphqlError";

export const PrivateRoute = () => {
    const [sidebarState, setSidebarState] = useState(false);

    const {data, loading, error, refetch} = useGetUser();
    const sessionContext = {user: data?.user, updateUser: refetch};

    if (!loading && !data && error && !isUnauthorized(error)) {
        return (
            <Box sx={{p: 3}}>
                <QueryErrorAlert error={error} fallback="Can not load your profile" onRetry={() => refetch()}/>
            </Box>
        );
    }

    return (
        loading ?
            <Loader/>
            :
            data ? (
                    <SessionContext.Provider value={sessionContext}>
                        <CountriesProvider>
                            <DialogProvider>
                                <MenuAppBar sidebarState={sidebarState} handleChangeState={setSidebarState}/>
                                <Box component="main" sx={{
                                    height: 100,
                                    flexGrow: 1,
                                    p: {xs: 1.5, sm: 3},
                                    marginLeft: sidebarState ? `${drawerWidth}px` : 7,
                                }}>
                                    <DrawerHeader/>
                                    <Outlet/>
                                </Box>
                            </DialogProvider>
                        </CountriesProvider>
                    </SessionContext.Provider>
                ) :
                (
                    <Navigate to="/" replace={true}/>
                )
    )
}