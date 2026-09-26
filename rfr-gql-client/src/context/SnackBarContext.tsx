import {Alert, Snackbar} from '@mui/material';
import {FC, ReactNode, useState} from 'react';
import {SnackBarContext} from './useSnackBar';

interface SnackBarContextProviderProps {
    children: ReactNode;
}

const SnackBarProvider: FC<SnackBarContextProviderProps> = ({children}) => {
    const [open, setOpen] = useState<boolean>(false);
    const [message, setMessage] = useState<string>('');
    const [typeColor, setTypeColor] = useState<"error" | "success" | "info">("success");

    const showSnackBar = (text: string, color: "error" | "success" | "info") => {
        setMessage(text);
        setTypeColor(color);
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    return (
        <SnackBarContext.Provider value={{showSnackBar}}>
            <Snackbar
                open={open}
                autoHideDuration={3000}
                anchorOrigin={{vertical: 'bottom', horizontal: 'right'}}
                onClose={handleClose}>
                <Alert onClose={handleClose} severity={typeColor}>
                    {message}
                </Alert>
            </Snackbar>
            {children}
        </SnackBarContext.Provider>
    );
};

export {SnackBarProvider};