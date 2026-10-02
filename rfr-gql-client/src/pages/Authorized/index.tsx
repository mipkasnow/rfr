import {useEffect} from "react";
import {Loader} from "../../components/Loader"
import {useNavigate, useSearchParams} from "react-router";
import {getTokenFromUrlEncodedParams, saveTokens} from "../../api/authUtils";
import {authClient} from "../../api/authClient";

export const AuthorizedPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        const getToken = async (data: URLSearchParams) => {
            const res = await authClient.getToken(data);
            if (res?.access_token && res?.id_token) {
                saveTokens(res.access_token, res.id_token);
                setTimeout(async () => {
                    navigate("/", {replace: true});
                }, 500);
            } else {
                console.log("Failed to get token");
                navigate("/");
            }
        };

        const code = searchParams?.get("code");
        const verifier = localStorage.getItem("codeVerifier");
        if (code && verifier) {
            const data = getTokenFromUrlEncodedParams(code, verifier);
            getToken(data);
        } else {
            console.log("Can not login to Cabinet");
            navigate("/");
        }
    }, [navigate, searchParams]);

    return (
        <Loader/>
    )
}