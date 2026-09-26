import {Card, Container} from "@mui/material"
import {ProfileForm} from "../../components/ProfileForm"

export const ProfilePage = () => {
    return (
        <Container maxWidth="lg" sx={{pb: 6}}>
            <Card sx={{p: {xs: 2, md: 5}}}>
                <ProfileForm/>
            </Card>
        </Container>
    )
}
