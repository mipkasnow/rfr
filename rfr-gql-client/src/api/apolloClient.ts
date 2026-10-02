import {ApolloClient, ApolloLink, CombinedGraphQLErrors, HttpLink, InMemoryCache, ServerError} from "@apollo/client";
import {SetContextLink} from "@apollo/client/link/context";
import {ErrorLink} from "@apollo/client/link/error";
import {accessTokenFromLocalStorage, clearSession} from "./authUtils";


const API_URL = `${import.meta.env.VITE_API_URL}`;

const apolloHttpLink = new HttpLink({
    uri: `${API_URL}/graphql`,
})

const headerLink = new SetContextLink((previousContext) => {
    const accessToken = accessTokenFromLocalStorage();
    return {
        headers: {
            ...previousContext.headers,
            ...(accessToken ? {"Authorization": `Bearer ${accessToken}`} : {}),
        },
    };
});

const errorLink = new ErrorLink(({error, operation}) => {
    if (ServerError.is(error) && error.statusCode === 401) {
        clearSession();
        if (window.location.pathname !== "/") {
            window.location.replace("/");
        }
        return;
    }
    if (import.meta.env.DEV) {
        if (CombinedGraphQLErrors.is(error)) {
            error.errors.forEach((e) => console.error(`[GraphQL error] ${operation.operationName}:`, e.message, e.extensions));
        } else {
            console.error(`[Network error] ${operation.operationName}:`, error);
        }
    }
});

export const apiClient = new ApolloClient({
    link: ApolloLink.from([errorLink, headerLink, apolloHttpLink]),
    cache: new InMemoryCache({
        typePolicies: {
            Feed: {
                merge: true,
            },
        },
    }),
});
