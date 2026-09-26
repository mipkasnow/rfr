import {ToggleButton, ToggleButtonGroup} from "@mui/material"
import {FC, MouseEvent} from "react";
import PersonOutlineRoundedIcon from '@mui/icons-material/PersonOutlineRounded';
import GroupOutlinedIcon from '@mui/icons-material/GroupOutlined';

interface ToggleInterface {
    withMyFriends: boolean,
    setWithMyFriends: (withMyFriends: boolean) => void;
}

export const Toggle: FC<ToggleInterface> = ({withMyFriends, setWithMyFriends}) => {

    const handleChange = (
        _event: MouseEvent<HTMLElement>,
        newFilter: "my" | "friends" | null,
    ) => {
        if (newFilter) {
            setWithMyFriends(newFilter === "friends");
        }
    };

    return (
        <ToggleButtonGroup
            size="small"
            value={withMyFriends ? "friends" : "my"}
            exclusive
            onChange={handleChange}
            aria-label="Travels filter"
        >
            <ToggleButton value="my">
                <PersonOutlineRoundedIcon fontSize="small"/>
                Only my travels
            </ToggleButton>
            <ToggleButton value="friends">
                <GroupOutlinedIcon fontSize="small"/>
                With friends
            </ToggleButton>
        </ToggleButtonGroup>
    )
}
