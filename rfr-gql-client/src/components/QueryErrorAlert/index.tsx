import {ErrorLike} from "@apollo/client";
import {Alert, Button} from "@mui/material";
import {FC} from "react";
import {errorMessage} from "../../api/graphqlError";

interface QueryErrorAlertInterface {
    error?: ErrorLike;
    fallback?: string;
    onRetry?: () => void;
}

export const QueryErrorAlert: FC<QueryErrorAlertInterface> = ({
                                                                  error,
                                                                  fallback = "Can not load data",
                                                                  onRetry
                                                              }) => {
    if (!error) {
        return null;
    }
    return (
        <Alert
            severity="error"
            sx={{marginBottom: 2}}
            action={onRetry && <Button color="inherit" size="small" onClick={onRetry}>Retry</Button>}
        >
            {errorMessage(error, fallback)}
        </Alert>
    );
};
