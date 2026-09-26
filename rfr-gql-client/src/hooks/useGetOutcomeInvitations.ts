import {gql} from "@apollo/client";
import {useQuery} from "@apollo/client/react";
import {Connection} from "../types/Connection";
import {User} from "../types/User";

type QueryData = {
    user: {
        outcomeInvitations: Connection<User>;
    };
};

const GET_OUTCOME_INVITATIONS = gql(`
    query GetOutcomeInvitations($page: Int, $size: Int, $searchQuery: String) {
        user {
            id
            outcomeInvitations(page: $page, size: $size, searchQuery: $searchQuery) {
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
export const useGetOutcomeInvitations = (req: getInvitationsRequestType) => {
    const {data, loading, error, refetch} = useQuery<QueryData>(GET_OUTCOME_INVITATIONS, {
        variables: {
            page: req.page ?? 0,
            size: 10,
            searchQuery: req.search ?? ""
        },
        fetchPolicy: "cache-and-network",
    });
    return {
        data: data?.user?.outcomeInvitations?.edges?.map((e) => e.node) ?? [],
        hasPreviousPage: data?.user?.outcomeInvitations?.pageInfo?.hasPreviousPage ?? false,
        hasNextPage: data?.user?.outcomeInvitations?.pageInfo?.hasNextPage ?? false,
        loading,
        error,
        refetch,
    };
}