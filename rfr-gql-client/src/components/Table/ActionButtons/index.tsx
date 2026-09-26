import {FC} from "react";
import {useSnackBar} from "../../../context/useSnackBar";
import {useUpdateFriendshipStatus} from "../../../hooks/useUpdateFriendshipStatus";
import {Box, Button, Chip} from "@mui/material";
import AddOutlinedIcon from '@mui/icons-material/AddOutlined';
import RemoveOutlinedIcon from '@mui/icons-material/RemoveOutlined';
import {errorMessage} from "../../../api/graphqlError";

interface ActionButtonsInterface {
    userId: string;
    friendStatus?: "NOT_FRIEND" | "FRIEND" | "INVITATION_SENT" | "INVITATION_RECEIVED";
}

type FriendshipAction = "ADD" | "ACCEPT" | "REJECT" | "DELETE";

export const ActionButtons: FC<ActionButtonsInterface> = ({userId, friendStatus}) => {
    const snackbar = useSnackBar();

    const {updateFriendship} = useUpdateFriendshipStatus();

    const runAction = (action: FriendshipAction, successMessage: string, errorFallback: string) => {
        updateFriendship({
            variables: {
                input: {
                    user: userId,
                    action,
                }
            },
            onCompleted: () => snackbar.showSnackBar(successMessage, "success"),
            onError: (e) => snackbar.showSnackBar(errorMessage(e, errorFallback), "error"),
        });
    }

    const handleAddUser = () => runAction("ADD", "Invitation sent", "Can not send invitation");
    const handleAcceptInvitation = () => runAction("ACCEPT", "Invitation accepted", "Can not accept invitation");
    const handleDeclineInvitation = () => runAction("REJECT", "Invitation declined", "Can not decline invitation");
    const handleDeleteFriend = () => runAction("DELETE", "Friend deleted", "Can not delete friend");
    const handleCancelInvitation = () => runAction("DELETE", "Invitation cancelled", "Can not cancel invitation");

    if (!friendStatus || friendStatus === "NOT_FRIEND") {
        return (
            <Button
                startIcon={<AddOutlinedIcon/>}
                type="button"
                variant="outlined"
                size="small"
                onClick={handleAddUser}
                sx={{
                    width: 100
                }}
            >
                Add
            </Button>
        )
    }

    return (
        <Box sx={{display: "inline-flex", alignItems: "center", flexWrap: "wrap", justifyContent: "flex-end", gap: 1}}>
            {
                friendStatus === "FRIEND" && (
                    <Button
                        startIcon={<RemoveOutlinedIcon/>}
                        type="button"
                        variant="outlined"
                        color="error"
                        size="small"
                        onClick={handleDeleteFriend}
                        sx={{
                            width: 100
                        }}
                    >
                        Remove
                    </Button>
                )}
            {
                friendStatus === "INVITATION_SENT" && (
                    <>
                        <Chip
                            sx={{

                                width: 100
                            }}
                            label="Waiting..."
                        />
                        <Button
                            startIcon={<RemoveOutlinedIcon/>}
                            type="button"
                            variant="outlined"
                            color="error"
                            size="small"
                            onClick={handleCancelInvitation}
                            sx={{
                                width: 100
                            }}
                        >
                            Cancel
                        </Button>
                    </>
                )
            }
            {
                friendStatus === "INVITATION_RECEIVED" && (
                    <>
                        <Button
                            startIcon={<AddOutlinedIcon/>}
                            type="button"
                            variant="contained"
                            size="small"
                            sx={{

                                width: 100,
                            }}
                            onClick={handleAcceptInvitation}
                        >
                            Accept
                        </Button>
                        <Button
                            startIcon={<RemoveOutlinedIcon/>}
                            type="button"
                            variant="outlined"
                            color="error"
                            size="small"
                            onClick={handleDeclineInvitation}
                            sx={{
                                width: 100
                            }}
                        >
                            Decline
                        </Button>
                    </>
                )
            }
        </Box>
    )
}
