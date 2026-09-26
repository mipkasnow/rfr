import {createContext, useContext} from "react";
import {PhotoFormProps} from "../components/PhotoModal/formValidate.ts";

export interface DialogDataInterface {
    title: string,
    formData: PhotoFormProps,
    isEdit: boolean,
    withFriends: boolean,
}

export interface DialogContextActions {
    showDialog: (dialogData: DialogDataInterface) => void;
}

export const DialogContext = createContext({} as DialogContextActions);

export const useDialog = (): DialogContextActions => {
    const context = useContext(DialogContext);

    if (!context) {
        throw new Error('useDialog must be used within an DialogProvider');
    }

    return context;
};
