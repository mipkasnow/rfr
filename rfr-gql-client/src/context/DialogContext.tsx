import * as React from 'react';
import {
    ChangeEvent,
    FC,
    FormEvent,
    forwardRef,
    ReactElement,
    ReactNode,
    useState
} from 'react';
import DialogMui from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import Button from "@mui/material/Button";
import {TransitionProps} from "@mui/material/transitions";
import Slide from "@mui/material/Slide";
import {
    Box,
    FormControl,
    Grid,
    InputLabel,
    MenuItem,
    OutlinedInput,
    Select,
    SelectChangeEvent,
    TextField
} from "@mui/material";
import {ImageUpload} from "../components/ImageUpload";
import {
    formHasErrors,
    formInitialState,
    formValidate,
    PhotoFormField,
    PhotoFormProps
} from "../components/PhotoModal/formValidate.ts";
import {MenuProps} from "../components/CountrySelect/menuProps.ts";
import {useCountries} from "./useCountries.ts";
import {useCreatePhoto} from "../hooks/useCreatePhoto.ts";
import {useSnackBar} from "./useSnackBar.ts";
import {DialogContext, DialogDataInterface} from "./useDialog.ts";
import {useUpdatePhoto} from "../hooks/useUpdatePhoto.ts";
import {errorMessage} from "../api/graphqlError.ts";

const Transition = forwardRef(function Transition(
    props: TransitionProps & {
        children: ReactElement;
    },
    ref: React.Ref<unknown>,
) {
    return <Slide direction="up" ref={ref} {...props} />;
});

interface DialogContextProps {
    children: ReactNode;
}

const DialogProvider: FC<DialogContextProps> = ({children}) => {
    const [open, setOpen] = useState<boolean>(false);
    const [dialogData, setDialogData] = useState<DialogDataInterface>();
    const [formValues, setFormValues] = useState<PhotoFormProps>(formInitialState);
    const {countries} = useCountries();
    const snackbar = useSnackBar();

    const {createPhoto} = useCreatePhoto({
        onError: (e) => snackbar.showSnackBar(errorMessage(e, "Can not create new post"), "error"),
        onCompleted: () => snackbar.showSnackBar("New post created", "success"),
        withFriends: dialogData?.withFriends ?? false,
    });

    const {updatePhoto} = useUpdatePhoto({
        onError: (e) => snackbar.showSnackBar(errorMessage(e, "Can not update post"), "error"),
        onCompleted: () => snackbar.showSnackBar("Post updated", "success"),
    });


    const showDialog = (dialogData: DialogDataInterface) => {
        setDialogData(dialogData);
        setFormValues(dialogData.formData);
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    const handleCreate = (e: FormEvent) => {
        e.preventDefault();
        const validatedData = formValidate(formValues);
        setFormValues(validatedData);
        if (!formHasErrors(validatedData)) {
            createPhoto({
                variables: {
                    input: {
                        src: formValues.src.value || "",
                        description: formValues.description.value,
                        country: {
                            code: formValues.country.value,
                        }
                    }
                }
            });
            handleClose();
            setOpen(false);
        }
    }

    const handleUpdate = (e: FormEvent) => {
        e.preventDefault();
        const validatedData = formValidate(formValues);
        setFormValues(validatedData);
        if (!formHasErrors(validatedData)) {
            updatePhoto({
                variables: {
                    input: {
                        id: dialogData?.formData.id || "",
                        src: formValues.src.value || "",
                        description: formValues.description.value,
                        country: {
                            code: formValues.country.value,
                        }
                    }
                }
            });
            handleClose();
        }
        setOpen(false);
    }

    const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
        const {value} = event.target;
        const name = event.target.name as PhotoFormField;
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
        const name = event.target.name as PhotoFormField;
        setFormValues({
            ...formValues,
            [name]: {
                ...formValues[name],
                value,
            }
        })
    };

    return (
        <DialogContext.Provider value={{showDialog}}>
            {children}
            <DialogMui
                open={open}
                slots={{transition: Transition}}
                keepMounted
                onClose={handleClose}
                aria-describedby="alert-dialog-slide-description"
            >
                <DialogTitle>{dialogData?.title}</DialogTitle>
                <DialogContent sx={{display: "flex", alignItems: "center"}}>
                    <Grid container spacing={2} component="form" noValidate
                          onSubmit={dialogData?.isEdit ? handleUpdate : handleCreate}>
                        <Grid size={12}>
                            <ImageUpload
                                buttonText="Upload new image"
                                file={formValues.src.value}
                                error={formValues.src.error}
                                helperText={formValues.src.error ? formValues.src.errorMessage : ""}
                                onFileUpload={(file) => {
                                    setFormValues({
                                        ...formValues, src: {
                                            ...formValues.src,
                                            value: file,
                                        }
                                    })
                                }}/>
                        </Grid>
                        <Grid size={12}>
                            <FormControl sx={{width: "100%"}}>
                                <InputLabel id="select-country-label">Country</InputLabel>
                                <Select
                                    id="country"
                                    name="country"
                                    labelId="select-country-label"
                                    value={formValues.country.value}
                                    onChange={handleSelectValueChange}
                                    fullWidth
                                    input={
                                        <OutlinedInput
                                            id="select-country"
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
                        <Grid size={12}>
                            <TextField
                                id="description"
                                name="description"
                                placeholder="About this photo"
                                label="Description"
                                type="text"
                                value={formValues.description.value}
                                onChange={handleChange}
                                error={formValues.description.error}
                                helperText={formValues.description.error && formValues.description.errorMessage}
                                fullWidth
                                multiline
                                maxRows={4}
                            />
                        </Grid>
                        <Box
                            sx={{
                                width: "100%",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "space-between",
                            }}
                        >
                            <Button variant="contained" sx={{margin: 2}} type="submit">Save</Button>
                            <Button sx={{margin: 2}} onClick={handleClose}>Close</Button>
                        </Box>
                    </Grid>
                </DialogContent>
            </DialogMui>
        </DialogContext.Provider>
    );
};

export {DialogProvider};
