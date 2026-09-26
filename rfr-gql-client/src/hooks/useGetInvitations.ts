import {gql} from "@apollo/client";
import {useQuery} from "@apollo/client/react";
import {Connection} from "../types/Connection";
import {User} from "../types/User";

type QueryData = {
    user: {
        incomeInvitations: Connection<User>;
    };
};

const GET_INVITATIONS = gql(`
    query GetInvitations($page: Int, $size: Int, $searchQuery: String) {
        user {
            id
            incomeInvitations(page: $page, size: $size, searchQuery: $searchQuery) {
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

type getInvitationsRequestType = {
    page: number,
    search: string,
}
export const useGetInvitations = (req: getInvitationsRequestType) => {
    const {data, loading, error, refetch} = useQuery<QueryData>(GET_INVITATIONS, {
        variables: {
            page: req.page ?? 0,
            size: 10,
            searchQuery: req.search ?? "",
        },
        fetchPolicy: "cache-and-network",
    });
    return {
        data: data?.user?.incomeInvitations?.edges?.map((e) => e.node) ?? [],
        hasPreviousPage: data?.user?.incomeInvitations?.pageInfo?.hasPreviousPage ?? false,
        hasNextPage: data?.user?.incomeInvitations?.pageInfo?.hasNextPage ?? false,
        loading,
        error,
        refetch,
    };
}