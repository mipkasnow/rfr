const AUTH_URL = `${import.meta.env.VITE_AUTH_URL}`;

type TokenResponse = {
    access_token?: string,
    id_token?: string,
    token_type?: string,
    expires_in?: number,
    scope?: string,
}

export const authClient = {
    getToken: async (data: URLSearchParams): Promise<TokenResponse> => {
        const response = await fetch(`${AUTH_URL}/oauth2/token`, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-type": "application/x-www-form-urlencoded"
            },
            body: data.toString()
        });
        if (!response.ok) {
            throw new Error("Failed loading data");
        }
        return response.json();
    },
    revokeToken: async (data: URLSearchParams): Promise<void> => {
        const response = await fetch(`${AUTH_URL}/oauth2/revoke`, {
            method: "POST",
            headers: {
                "Content-type": "application/x-www-form-urlencoded"
            },
            body: data.toString()
        });
        if (!response.ok) {
            throw new Error(`Failed to revoke token: ${response.status}`);
        }
    },
}
