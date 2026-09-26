import {createContext, useContext} from 'react';

export type SnackBarContextActions = {
    showSnackBar: (text: string, typeColor: "error" | "success" | "info") => void;
};

export const SnackBarContext = createContext({} as SnackBarContextActions);

export const useSnackBar = (): SnackBarContextActions => {
    const context = useContext(SnackBarContext);

    if (!context) {
        throw new Error('useSnackBar must be used within an SnackBarProvider');
    }

    return context;
};
