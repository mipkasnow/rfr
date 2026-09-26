import {Box, Button, Card, Divider, Typography} from "@mui/material";
import deerLogo from "./../../assets/deer-logo.svg";
import "./styles.css";
import {Navigate} from "react-router";
import {getRegisterLink, initLocalStorageAndRedirectToAuth} from "../../api/authUtils";
import {Loader} from "../../components/Loader";
import {useGetUser} from "../../hooks/useGetUser";

export const LandingPage = () => {
    const {data, loading} = useGetUser();
    const onLoginClick = () => {
        initLocalStorageAndRedirectToAuth();
    }

    return (
        loading ?
            (<Loader/>) :
            data ?
                (
                    <Navigate to="/my-travels" replace={true}/>
                ) :
                (
                    <Box className="landing__wrapper">
                        <Card className="landing__container" sx={{borderRadius: {xs: 0, sm: "16px"}, border: {xs: 0, sm: 1}, borderColor: {sm: "divider"}}}>
                            <Box className="landing__hero"/>
                            <Box className="landing__content">
                                <Typography variant="h4" component="h2" className="landing__header">
                                    <Box
                                        component="span"
                                        className="landing__logo"
                                        role="img"
                                        aria-label="Rangiffler logo"
                                        sx={{maskImage: `url("${deerLogo}")`, WebkitMaskImage: `url("${deerLogo}")`}}
                                    />
                                    <span><Box component="span" sx={{color: "primary.main"}}>R</Box>angiffler</span>
                                </Typography>
                                <Typography sx={{color: "text.secondary", mt: 0.5, mb: 4}}>
                                    Share your best places with Rangiffler
                                </Typography>
                                <Button
                                    variant="contained"
                                    size="large"
                                    fullWidth
                                    onClick={onLoginClick}
                                >
                                    Login
                                </Button>
                                <Divider sx={{my: 3, color: "text.secondary", fontSize: 13}}>New here?</Divider>
                                <Typography variant="body2" sx={{color: "text.secondary", mb: 2, textAlign: "center"}}>
                                    If you don't have account, we're waiting for you to join our journey
                                </Typography>
                                <Button
                                    variant="outlined"
                                    size="large"
                                    fullWidth
                                    component="a"
                                    href={getRegisterLink()}>
                                    Register
                                </Button>
                            </Box>
                        </Card>
                    </Box>
                )
    );
};
