import {ErrorLike, gql} from "@apollo/client";
import {useMutation} from "@apollo/client/react";

interface PhotoInput {
    variables: {
        input: {
            id: string,
            like: {
                user: string
            }
        }
    }
}

const LIKE_PHOTO = gql(`
    mutation LikePhoto($input: PhotoInput!) {
        photo(input: $input) {
            id
            country {
                code
                name
                flag
            }
            description
            likes {
                total
                 likes {
                    user 
                }
            }
        }
    }
`);

type LikePhotoRequestType = {
    onError: (error: ErrorLike) => void,
    onCompleted: () => void,
}

type LikePhotoReturnType = {
    likePhoto: (updateUserInput: PhotoInput) => void,
    loading: boolean,
}

export const useLikePhoto = (req: LikePhotoRequestType): LikePhotoReturnType => {
    const [likePhoto, {loading}] = useMutation(LIKE_PHOTO, {
        onError: req.onError,
        onCompleted: req.onCompleted,
    });
    return {likePhoto, loading};
};