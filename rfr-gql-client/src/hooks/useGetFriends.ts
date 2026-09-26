import {gql} from "@apollo/client";
import {useQuery} from "@apollo/client/react";
import {Connection} from "../types/Connection";
import {User} from "../types/User";

type QueryData = {
    user: {
        friends: Connection<User>;
    };
};

const GET_FRIENDS = gql(`
    query GetFriends($page: Int, $size: Int, $searchQuery: String) {
        user {
            id
            friends(page: $page, size: $size, searchQuery: $searchQuery) {
                edges {
                    node {
                        id
                        username
                        firstname
                        surname
                        avatar
                        location {
                            code
                            name
                            flag
                        }
                        friendStatus
                    }
                }
                pageInfo {
                    hasPreviousPage
                    hasNextPage
                }        
            }
        }
    }
`);

type getFriendsRequestType = {
    page: number,
    search: string,
}
export const useGetFriends = (req: getFriendsRequestType) => {
    const {data, loading, error, refetch} = useQuery<QueryData>(GET_FRIENDS, {
        variables: {
            page: req.page ?? 0,
            size: 10,
            searchQuery: req.search ?? "",
        },
        fetchPolicy: "cache-and-network",
    });
    return {
        data: data?.user?.friends?.edges?.map((e) => e.node) ?? [],
        hasPreviousPage: data?.user?.friends?.pageInfo?.hasPreviousPage ?? false,
        hasNextPage: data?.user?.friends?.pageInfo?.hasNextPage ?? false,
        loading,
        error,
        refetch,
    };
}