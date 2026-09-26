import {createContext, useContext} from "react";
import {Country} from "../types/Country";

export type CountriesContextData = {
    countries: Country[];
};

export const CountriesContext = createContext({} as CountriesContextData);

export const useCountries = (): CountriesContextData => {
    const context = useContext(CountriesContext);

    if (!context) {
        throw new Error('useCountries must be used within an CountriesProvider');
    }

    return context;
};
