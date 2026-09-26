import * as crypto from "crypto-js";
import sha256 from "crypto-js/sha256";
import Base64 from "crypto-js/enc-base64";

const AUTH_URL = `${import.meta.env.VITE_AUTH_URL}`;
const FRONT_URL = `${import.meta.env.VITE_FRONT_URL}`;
const CLIENT_ID = `${import.meta.env.VITE_CLIENT_ID}`;

const base64Url = (str: string | crypto.lib.WordArray) => {
    return str.toString(Base64).replace(/=/g, "").replace(/\+/g, "-").replace(/\//g, "_");
}

const generateCodeVerifier = () => {
    return base64Url(crypto.enc.Base64.stringify(crypto.lib.WordArray.random(32)));
}

const generateCodeChallenge = () => {
    const codeVerifier = localStorage.getItem("codeVerifier");
    return base64Url(sha256(codeVerifier!));
}

const currentTheme = (): "light" | "dark" => {
    const mode = localStorage.getItem("mui-mode");
    if (mode === "system") {
        return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    }
    return mode === "dark" ? "dark" : "light";
}

const getAuthLink = (codeChallenge: string) => {
    return `${AUTH_URL}/oauth2/authorize?response_type=code&client_id=${CLIENT_ID}&scope=openid&redirect_uri=${FRONT_URL}/authorized&code_challenge=${codeChallenge}&code_challenge_method=S256&theme=${currentTheme()}`
}

const getRegisterLink = () => {
    return `${AUTH_URL}/register?theme=${currentTheme()}`
}

const getLogoutLink = (token: string) => {
    return `${AUTH_URL}/connect/logout?id_token_hint=${token}&post_logout_redirect_uri=${FRONT_URL}/logout&theme=${currentTheme()}`
}

const idTokenFromLocalStorage = (): string => {
    return <string>localStorage.getItem('id_token');
}

const getTokenFromUrlEncodedParams = (code: string, verifier: string) => {
    return new URLSearchParams({
        "code": code,
        "redirect_uri": `${FRONT_URL}/authorized`,
        "code_verifier": verifier,
        "grant_type": "authorization_code",
        "client_id": `${CLIENT_ID}`,
    });
}

const initLocalStorageAndRedirectToAuth = () => {
    const codeVerifier = generateCodeVerifier();
    localStorage.setItem('codeVerifier', codeVerifier);
    const codeChallenge = generateCodeChallenge();
    localStorage.setItem('codeChallenge', codeChallenge);

    const link = getAuthLink(codeChallenge);
    window.location.replace(link);
}

const clearSession = () => {
    localStorage.removeItem('codeVerifier');
    localStorage.removeItem('codeChallenge');
    localStorage.removeItem('id_token');
}

export {
    generateCodeChallenge,
    generateCodeVerifier,
    getAuthLink,
    getLogoutLink,
    getRegisterLink,
    idTokenFromLocalStorage,
    getTokenFromUrlEncodedParams,
    clearSession,
    initLocalStorageAndRedirectToAuth
};
