import {ErrorLike, gql} from "@apollo/client";
import {useMutation} from "@apollo/client/react";

interface UserInput {
    variables: {
        input: {
            firstname: string,
            surname: string,
            avatar?: string,
            location: {
                code: string,
            }
        }
    }
}

const UPDATE_USER = gql(`
    mutation UpdateUser($input: UserInput!){
        user(input: $input) {
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

type UpdateUserRequestType = {
    onError: (error: ErrorLike) => void,
    onCompleted: () => void,
}

type UpdateUserReturnType = {
    updateUser: (updateUserInput: UserInput) => void,
    loading: boolean,
}
export const useUpdateUser = (req: UpdateUserRequestType): UpdateUserReturnType => {
    const [updateUser, {loading}] = useMutation(UPDATE_USER, {
        onError: req.onError,
        onCompleted: req.onCompleted,
    });
    return {updateUser, loading};
};