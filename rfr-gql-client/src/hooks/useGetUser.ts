import {gql} from "@apollo/client";
import {useQuery} from "@apollo/client/react";
import {User} from "../types/User";

type GetUserData = {
    user: User;
};

export const GET_USER = gql(`
    query GetUser {
        user {
            id
            username
            firstname
            surname
            avatar
            location {
                code
                name
            }
        }
    }
`);

export const useGetUser = () => {
    const {data, loading, error, refetch} = useQuery<GetUserData>(GET_USER);
    return {
        data,
        loading,
        error,
        refetch,
    };
}