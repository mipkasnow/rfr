import {ErrorLike, gql} from "@apollo/client";
import {useMutation} from "@apollo/client/react";

interface PhotoInput {
    variables: {
        input: {
            id: string,
            src?: string,
            description: string,
            country: {
                code: string,
            }
        }
    }
}

const UPDATE_PHOTO = gql(`
    mutation UpdatePhoto($input: PhotoInput!) {
        photo(input: $input) {
            id
            src
            country {
                code
                name
                flag
            }
            description
            likes {
                total
            }
        }
    }
`);

type UpdatePhotoRequestType = {
    onError: (error: ErrorLike) => void,
    onCompleted: () => void,
}

type UpdatePhotoReturnType = {
    updatePhoto: (updatePhotoInput: PhotoInput) => void,
    loading: boolean,
}

export const useUpdatePhoto = (req: UpdatePhotoRequestType): UpdatePhotoReturnType => {
    const [updatePhoto, {loading}] = useMutation(UPDATE_PHOTO, {
        refetchQueries: ["GetFeed"],
        onError: req.onError,
        onCompleted: req.onCompleted,
    });
    return {updatePhoto, loading};
};