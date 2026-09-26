import {gql} from "@apollo/client";
import {useQuery} from "@apollo/client/react";
import {Connection} from "../types/Connection";
import {User} from "../types/User";

type GetPeopleData = {
    users: Connection<User>;
};

const GET_PEOPLE = gql(`
    query GetPeople($page: Int, $size: Int, $searchQuery: String) {
        users(page: $page, size: $size, searchQuery: $searchQuery) {
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
`);

type getPeopleRequestType = {
    page: number,
    search: string,
}

export const useQueryPeople = (req: getPeopleRequestType) => {
    const {data, loading, error, refetch} = useQuery<GetPeopleData>(GET_PEOPLE, {
        variables: {
            page: req.page ?? 0,
            size: 10,
            searchQuery: req.search ?? "",
        },
        fetchPolicy: "cache-and-network",
    });

    return {
        data: data?.users?.edges?.map((e) => e.node) ?? [],
        hasPreviousPage: data?.users?.pageInfo?.hasPreviousPage ?? false,
        hasNextPage: data?.users?.pageInfo?.hasNextPage ?? false,
        loading,
        error,
        refetch,
    };
}