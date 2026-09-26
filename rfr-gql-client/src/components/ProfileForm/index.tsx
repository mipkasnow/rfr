import {
    Box,
    Button,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    OutlinedInput,
    Select,
    SelectChangeEvent,
    TextField,
    Typography
} from "@mui/material"
import {useCountries} from "../../context/useCountries";
import {MenuProps} from "../CountrySelect/menuProps";
import {ChangeEvent, FormEvent, useContext, useState} from "react";
import {ImageUpload} from "../ImageUpload";
import {formInitialState, formValidate, UserFormProps} from "./formValidate";
import {formHasErrors} from "../PhotoModal/formValidate";
import {useUpdateUser} from "../../hooks/useUpdateUser";
import {useSnackBar} from "../../context/useSnackBar";
import {SessionContext} from "../../context/SessionContext";
import {errorMessage} from "../../api/graphqlError";

export const ProfileForm = () => {
    const {countries} = useCountries();
    const {user} = useContext(SessionContext);
    const snackbar = useSnackBar();
    const {updateUser} = useUpdateUser({
        onError: (e) => snackbar.showSnackBar(errorMessage(e, "Can not update user"), "error"),
        onCompleted: () => snackbar.showSnackBar("Your profile is successfully updated", "success"),
    });

    const getInitialState = () => ({
        ...formInitialState,
        firstname: {...formInitialState.firstname, value: user?.firstname ?? ""},
        surname: {...formInitialState.surname, value: user?.surname ?? ""},
        username: {...formInitialState.username, value: user?.username ?? ""},
        location: {...formInitialState.location, value: user?.location.code ?? "ru"},
        avatar: {...formInitialState.avatar, value: user?.avatar ?? undefined}
    });

    const [formValues, setFormValues] = useState<UserFormProps>(getInitialState());

    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        const {value} = event.target;
        const name = event.target.name as keyof UserFormProps;
        setFormValues({
            ...formValues,
            [name]: {
                ...formValues[name],
                value,
            }
        })
    };

    const handleSelectValueChange = (event: SelectChangeEvent<string>) => {
        const {value} = event.target;
        const name = event.target.name as keyof UserFormProps;
        setFormValues({
            ...formValues,
            [name]: {
                ...formValues[name],
                value,
            }
        })
    };

    const onSubmit = (e: FormEvent) => {
        e.preventDefault();
        const validatedData = formValidate(formValues);
        setFormValues(validatedData);
        if (!formHasErrors(validatedData)) {
            updateUser({
                variables: {
                    input: {
                        firstname: formValues.firstname.value,
                        surname: formValues.surname.value,
                        avatar: formValues.avatar.value,
                        location: {
                            code: formValues.location.value,
                        }
                    }
                }
            });
        }
    };

    const onReset = () => {
        setFormValues(getInitialState());
    };

    return (
        <Grid
            container
            component="form"
            onSubmit={onSubmit}
            spacing={0}
            sx={{
                rowGap: 4,
            }}
        >
            <Grid size={{xs: 12, md: 4}} sx={{
                display: "flex",
                justifyContent: "center",
            }}>
                <ImageUpload
                    buttonText="Update avatar"
                    file={formValues.avatar.value}
                    error={formValues.avatar.error}
                    helperText={formValues.avatar.error ? formValues.avatar.errorMessage : ""}
                    isAvatar
                    onFileUpload={(file) => {
                        setFormValues({
                            ...formValues, avatar: {
                                ...formValues.avatar,
                                value: file,
                            }
                        })
                    }}/>
            </Grid>
            <Grid size={{xs: 12, md: 8}}>
                <Typography
                    variant="h4"
                    component="h2"
                    sx={{
                        marginBottom: 3,
                    }}
                >
                    My profile
                </Typography>
                <Grid
                    container
                    spacing={3}
                >
                    <Grid size={{xs: 12, sm: 6}}>
                        <TextField
                            id="firstname"
                            name="firstname"
                            label="First Name"
                            type="text"
                            value={formValues?.firstname.value}
                            error={formValues.firstname.error}
                            helperText={formValues.firstname.error && formValues.firstname.errorMessage}
                            onChange={handleChange}
                            fullWidth
                        />
                    </Grid>
                    <Grid size={{xs: 12, sm: 6}}>
                        <TextField
                            id="surname"
                            name="surname"
                            label="Surname"
                            type="text"
                            value={formValues.surname.value}
                            error={formValues.surname.error}
                            helperText={formValues.surname.error && formValues.surname.errorMessage}
                            onChange={handleChange}
                            fullWidth
                        />
                    </Grid>
                    <Grid size={12}>
                        <TextField
                            id="username"
                            name="username"
                            label="Username"
                            type="text"
                            value={formValues.username.value}
                            error={formValues.username.error}
                            helperText={formValues.username.error && formValues.username.errorMessage}
                            disabled
                            fullWidth
                        />
                    </Grid>
                    <Grid size={12}>
                        <FormControl sx={{width: "100%"}}>
                            <InputLabel id="select-location-label">Location</InputLabel>
                            <Select
                                id="location"
                                name="location"
                                labelId="select-location-label"
                                value={formValues.location.value}
                                onChange={handleSelectValueChange}
                                fullWidth
                                input={
                                    <OutlinedInput
                                        id="select-location"
                                        label="Location"
                                        multiline={false}
                                    />}
                                MenuProps={MenuProps}
                            >
                                {countries.map((option) => (
                                    <MenuItem key={option.code} value={option.code}>
                                        <img width={20} src={option.flag} alt={option.name}/>&nbsp;{option.name}
                                    </MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                    </Grid>
                    <Grid size={12} sx={{
                        margin: "0 auto",
                    }}>
                        <Box sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            width: "100%",
                        }}>
                            <Button
                                variant="outlined"
                                type="button"
                                onClick={onReset}
                                sx={{
                                    margin: 2,
                                    width: "100%",
                                }}
                            >Reset</Button>
                            <Button
                                variant="contained"
                                type="submit"
                                sx={{
                                    margin: 2,
                                    width: "100%",
                                }}
                            >Save</Button>
                        </Box>
                    </Grid>
                </Grid>
            </Grid>
        </Grid>
    )
}