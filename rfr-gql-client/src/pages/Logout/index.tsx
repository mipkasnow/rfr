import {Loader} from "../../components/Loader";
import {useEffect} from "react";
import {clearSession} from "../../api/authUtils.ts";

export const LogoutPage = () => {

    useEffect(() => {
        clearSession();
        window.location.replace("/");
    }, []);

    return (
        <Loader/>
    )
}