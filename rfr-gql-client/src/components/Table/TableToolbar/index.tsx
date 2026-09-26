import {IconButton, InputBase, Paper, Toolbar} from "@mui/material";
import {ChangeEvent, FC, FormEvent, useState} from "react";
import SearchIcon from '@mui/icons-material/Search';


interface TableToolbarProps {
    setSearch: (value: string) => void;
    onSearchSubmit: () => void;
}

export const TableToolbar: FC<TableToolbarProps> = ({setSearch, onSearchSubmit}) => {
    const [value, setValue] = useState("");

    const handleSubmitSearch = (e: FormEvent) => {
        e.preventDefault();
        setSearch(value);
        onSearchSubmit();
    }

    return (
        <Toolbar disableGutters sx={{px: {xs: 2, sm: 3}, py: 2}}>
            <Paper
                component="form"
                variant="outlined"
                sx={{
                    p: '2px 4px 2px 12px',
                    display: 'flex',
                    alignItems: 'center',
                    width: "100%",
                    maxWidth: 480,
                    borderRadius: 999,
                    bgcolor: "action.hover",
                    borderColor: "transparent",
                    "&:focus-within": {borderColor: "primary.main", bgcolor: "background.paper"},
                }}
                onSubmit={handleSubmitSearch}
            >
                <SearchIcon sx={{color: "text.secondary", mr: 1}} fontSize="small"/>
                <InputBase
                    sx={{flex: 1}}
                    placeholder="Search people"
                    value={value}
                    onChange={(e: ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => setValue(e.target.value)}
                    inputProps={{'aria-label': 'search people'}}
                />
                <IconButton type="submit" size="small" color="primary" aria-label="search">
                    <SearchIcon/>
                </IconButton>
            </Paper>
        </Toolbar>
    );
}
