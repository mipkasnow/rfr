import {gql} from "@apollo/client";
import {useQuery} from "@apollo/client/react";
import {Country} from "../types/Country";

type GetCountriesData = {
    countries: Country[];
};

const GET_COUNTRIES = gql(`
    query GetCountries {
        countries {
            code
            name
            flag
        }
    }
`);

export const useGetCountries = () => {
    const {data, loading, error, refetch} = useQuery<GetCountriesData>(GET_COUNTRIES);
    return {
        data,
        loading,
        error,
        refetch,
    };
}