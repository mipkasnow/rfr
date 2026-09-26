import {Country} from "./Country";
import {Likes} from "./Likes";

export type Photo = {
    id: string;
    src: string;
    country: Country;
    description: string;
    isOwner: boolean;
    likes: Likes;

}