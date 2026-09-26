import {Avatar, Box, Button, Chip, List, ListItem, ListItemAvatar, ListItemButton, ListItemText, Tooltip, Typography} from "@mui/material";
import ExpandMoreRoundedIcon from '@mui/icons-material/ExpandMoreRounded';
import CheckRoundedIcon from '@mui/icons-material/CheckRounded';
import {FC, FocusEvent, KeyboardEvent, RefObject, SyntheticEvent, useRef, useState} from "react";
import {Stat} from "../../types/Stat";
import {useCountries} from "../../context/useCountries";

interface VisitedCountriesInterface {
    stat: Stat[];
    selectedCountry?: string | null;
    onSelect?: (countryCode: string) => void;
}

interface VisitedCountriesListInterface extends VisitedCountriesInterface {
    containerRef: RefObject<HTMLDivElement | null>;
}

const VisitedCountriesList: FC<VisitedCountriesListInterface> = ({stat, selectedCountry, onSelect, containerRef}) => {
    const {countries} = useCountries();
    const byCode = new Map((countries ?? []).map((c) => [c.code, c]));
    const rows = [...stat].sort((a, b) => b.count - a.count);

    return (
        <Box ref={containerRef} sx={{py: 1.5, minWidth: 260}} onMouseDown={(e) => e.preventDefault()}>
            <Typography variant="subtitle2" sx={{px: 2, pb: 0.5, fontWeight: 700}}>
                Photos per country
            </Typography>
            <List dense disablePadding sx={{maxHeight: 360, overflowY: "auto", px: 1}}>
                {rows.map(({country, count}) => {
                    const info = byCode.get(country.code);
                    return (
                        <ListItem
                            key={country.code}
                            disablePadding
                            secondaryAction={selectedCountry === country.code
                                ? <CheckRoundedIcon color="primary" fontSize="small" sx={{mr: 0.5}}/>
                                : <Chip size="small" label={count} color="primary" variant="outlined"/>}
                        >
                            <ListItemButton
                                selected={selectedCountry === country.code}
                                onClick={() => onSelect?.(country.code)}
                                sx={{borderRadius: "8px"}}
                            >
                            <ListItemAvatar sx={{minWidth: 40}}>
                                <Avatar src={info?.flag} alt="" sx={{width: 24, height: 24}}>
                                    {country.code.toUpperCase()}
                                </Avatar>
                            </ListItemAvatar>
                            <ListItemText
                                primary={info?.name ?? country.code.toUpperCase()}
                                slotProps={{primary: {sx: {fontWeight: 500}}}}
                            />
                            </ListItemButton>
                        </ListItem>
                    );
                })}
            </List>
        </Box>
    );
};

export const VisitedCountries: FC<VisitedCountriesInterface> = ({stat, selectedCountry, onSelect}) => {
    const [open, setOpen] = useState(false);
    const listRef = useRef<HTMLDivElement>(null);
    const label = `${stat.length} ${stat.length === 1 ? "country" : "countries"} visited`;

    const handleClose = (event: Event | SyntheticEvent) => {
        const next = (event as FocusEvent).relatedTarget as Node | null;
        if (next && listRef.current?.contains(next)) {
            return;
        }
        setOpen(false);
    };

    const focusFirstCountry = (event: KeyboardEvent) => {
        if (event.key === "ArrowDown") {
            event.preventDefault();
            setOpen(true);
            requestAnimationFrame(() => listRef.current?.querySelector<HTMLElement>("[role=button]")?.focus());
        }
    };

    if (!stat.length) {
        return (
            <Typography variant="body2" sx={{color: "text.secondary", mt: 0.5}}>
                {label}
            </Typography>
        );
    }

    return (
        <Tooltip
            title={
                <VisitedCountriesList
                    containerRef={listRef}
                    stat={stat}
                    selectedCountry={selectedCountry}
                    onSelect={(code) => {
                        setOpen(false);
                        onSelect?.(code);
                    }}
                />
            }
            open={open}
            onOpen={() => setOpen(true)}
            onClose={handleClose}
            placement="bottom-start"
            enterTouchDelay={0}
            leaveTouchDelay={4000}
            slotProps={{
                tooltip: {
                    sx: {
                        p: 0,
                        maxWidth: 360,
                        bgcolor: "background.paper",
                        color: "text.primary",
                        border: 1,
                        borderColor: "divider",
                        borderRadius: "12px",
                        boxShadow: "0 8px 24px rgba(16, 24, 20, 0.16)",
                    },
                },
            }}
        >
            <Button
                size="small"
                endIcon={<ExpandMoreRoundedIcon/>}
                aria-haspopup="true"
                aria-expanded={open}
                onClick={() => setOpen(true)}
                onKeyDown={focusFirstCountry}
                sx={{color: "text.secondary", fontWeight: 500, px: 1, ml: -1, mt: 0.25}}
            >
                {label}
            </Button>
        </Tooltip>
    );
};
