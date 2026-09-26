import {createTheme} from '@mui/material/styles';

// A custom theme for this app
const theme = createTheme({
    cssVariables: {
        colorSchemeSelector: 'class',
    },
    colorSchemes: {
        light: {
            palette: {
                primary: {
                    main: "#174536",
                },
                secondary: {
                    main: "#768c7d",
                    light: "#FAFAFD",
                },
                error: {
                    main: "#d32f2f",
                },
                background: {
                    default: "#f4f6f5",
                    paper: "#ffffff",
                },
            },
        },
        dark: {
            palette: {
                primary: {
                    main: "#7cc4a4",
                    contrastText: "#0b1a14",
                },
                secondary: {
                    main: "#9fb3a6",
                    light: "#2a3531",
                },
                error: {
                    main: "#f28b82",
                },
                background: {
                    default: "#0f1513",
                    paper: "#17201d",
                },
            },
        },
    },
    shape: {
        borderRadius: 12,
    },
    typography: {
        fontFamily: ['Roboto', 'Helvetica', 'Arial', 'sans-serif'].join(','),
        h4: {
            fontWeight: 700,
        },
        button: {
            fontWeight: 600,
        },
    },
    components: {
        MuiAppBar: {
            defaultProps: {
                elevation: 0,
            },
        },
        MuiButton: {
            defaultProps: {
                disableElevation: true,
            },
            styleOverrides: {
                root: {
                    textTransform: "none",
                    borderRadius: 10,
                },
                sizeMedium: {
                    padding: "10px 16px",
                },
            },
        },
        MuiTab: {
            styleOverrides: {
                root: {
                    textTransform: "none",
                    fontWeight: 600,
                    fontSize: "0.95rem",
                },
            },
        },
        MuiFab: {
            styleOverrides: {
                root: {
                    textTransform: "none",
                    fontWeight: 600,
                },
            },
        },
        MuiCard: {
            defaultProps: {
                elevation: 0,
            },
            styleOverrides: {
                root: ({theme}) => ({
                    borderRadius: 16,
                    border: `1px solid ${(theme.vars ?? theme).palette.divider}`,
                    boxShadow: "0 1px 2px rgba(16, 24, 20, 0.04), 0 4px 16px rgba(16, 24, 20, 0.06)",
                }),
            },
        },
        MuiPaper: {
            styleOverrides: {
                rounded: {
                    borderRadius: 12,
                },
            },
        },
        MuiDialog: {
            styleOverrides: {
                paper: {
                    borderRadius: 16,
                },
            },
        },
        MuiOutlinedInput: {
            styleOverrides: {
                root: {
                    borderRadius: 10,
                },
            },
        },
        MuiChip: {
            styleOverrides: {
                root: {
                    fontWeight: 500,
                },
            },
        },
        MuiToggleButtonGroup: {
            styleOverrides: {
                root: ({theme}) => ({
                    padding: 4,
                    gap: 4,
                    borderRadius: 999,
                    backgroundColor: (theme.vars ?? theme).palette.action.hover,
                }),
                grouped: {
                    border: 0,
                    margin: 0,
                },
                firstButton: {
                    borderRadius: 999,
                },
                middleButton: {
                    borderRadius: 999,
                },
                lastButton: {
                    borderRadius: 999,
                },
            },
        },
        MuiToggleButton: {
            styleOverrides: {
                root: ({theme}) => ({
                    gap: 8,
                    border: 0,
                    whiteSpace: "nowrap",
                    borderRadius: 999,
                    paddingInline: 16,
                    textTransform: "none",
                    fontWeight: 600,
                    color: (theme.vars ?? theme).palette.text.secondary,
                    "&.Mui-selected, &.Mui-selected:hover": {
                        color: (theme.vars ?? theme).palette.primary.main,
                        backgroundColor: (theme.vars ?? theme).palette.background.paper,
                        boxShadow: "0 1px 3px rgba(16, 24, 20, 0.16)",
                    },
                }),
            },
        },
        MuiTableCell: {
            styleOverrides: {
                head: ({theme}) => ({
                    fontWeight: 600,
                    color: (theme.vars ?? theme).palette.text.secondary,
                    backgroundColor: (theme.vars ?? theme).palette.action.hover,
                }),
            },
        },
    },
});

export default theme;
