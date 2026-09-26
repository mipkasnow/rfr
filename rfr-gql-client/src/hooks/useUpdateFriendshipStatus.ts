import {ErrorLike, gql} from "@apollo/client";
import {useApolloClient, useMutation} from "@apollo/client/react";

interface FriendshipInput {
    variables: {
        input: {
            user: string,
            action: "ADD" | "ACCEPT" | "REJECT" | "DELETE",
        }
    },
    onCompleted?: () => void,
    onError?: (error: ErrorLike) => void,
}

const FRIENDSHIP_ACTION = gql(`
      mutation FriendshipAction($input: FriendshipInput!) {
            friendship(input: $input) {
                id
                username
                friendStatus
            }
       }
`);

const PEOPLE_QUERIES = ["GetPeople", "GetFriends", "GetInvitations", "GetOutcomeInvitations"];

type UpdateFriendshipReturnType = {
    updateFriendship: (updateUserInput: FriendshipInput) => void,
    loading: boolean,
}
export const useUpdateFriendshipStatus = (): UpdateFriendshipReturnType => {
    const client = useApolloClient();
    const [mutate, {loading}] = useMutation(FRIENDSHIP_ACTION, {
        refetchQueries: PEOPLE_QUERIES,
        awaitRefetchQueries: true,
    });
    const updateFriendship = ({onError, ...options}: FriendshipInput) => {
        mutate({
            ...options,
            onError: (error) => {
                client.refetchQueries({include: PEOPLE_QUERIES});
                onError?.(error);
            },
        });
    };
    return {updateFriendship, loading};
};
