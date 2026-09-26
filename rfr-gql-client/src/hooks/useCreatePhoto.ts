import {ErrorLike, gql} from "@apollo/client";
import {useMutation} from "@apollo/client/react";
import {FEED_PAGE_SIZE, GET_FEED} from "./useGetFeed.ts";

interface PhotoInput {
    variables: {
        input: {
            src: string,
            description: string,
            country: {
                code: string,
            }
        }
    }
}

const CREATE_PHOTO = gql(`
    mutation CreatePhoto($input: PhotoInput!) {
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

type CreatePhotoRequestType = {
    withFriends: boolean,
    onError: (error: ErrorLike) => void,
    onCompleted: () => void,
}

type CreatePhotoReturnType = {
    createPhoto: (updateUserInput: PhotoInput) => void,
    loading: boolean,
}

export const useCreatePhoto = (req: CreatePhotoRequestType): CreatePhotoReturnType => {
    const [createPhoto, {loading}] = useMutation(CREATE_PHOTO, {
        onError: req.onError,
        onCompleted: req.onCompleted,
        refetchQueries: [{
            query: GET_FEED,
            variables: {
                withFriends: req.withFriends,
                page: 0,
                size: FEED_PAGE_SIZE,
            }
        }, "GetFeed"],
    });
    return {createPhoto, loading};
};