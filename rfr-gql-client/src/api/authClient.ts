const AUTH_URL = `${import.meta.env.VITE_AUTH_URL}`;

export const authClient = {
    getToken: async (data: URLSearchParams) => {
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
}