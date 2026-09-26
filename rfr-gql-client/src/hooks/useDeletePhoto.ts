import {ErrorLike, gql} from "@apollo/client";
import {useMutation} from "@apollo/client/react";

interface DeletePhotoInput {
    variables: {
        id: string;
    }
}

const DELETE_PHOTO = gql(`
    mutation DeletePhoto($id: ID!) {
        deletePhoto(id: $id) 
    }
`);

type DeletePhotoRequestType = {
    onError: (error: ErrorLike) => void,
    onCompleted: () => void,
}

type DeletePhotoReturnType = {
    deletePhoto: (data: DeletePhotoInput) => void,
    loading: boolean,
}

export const useDeletePhoto = (req: DeletePhotoRequestType): DeletePhotoReturnType => {
    const [deletePhoto, {loading}] = useMutation(DELETE_PHOTO, {
        onError: req.onError,
        onCompleted: req.onCompleted,
        refetchQueries: ["GetFeed"],
    });
    return {deletePhoto, loading};
};
