import {FC, ReactNode} from "react";
import {useGetCountries} from "../hooks/useGetCountries";
import {CountriesContext} from "./useCountries";

interface CountriesContextProviderProps {
    children: ReactNode;
}

const CountriesProvider: FC<CountriesContextProviderProps> = ({children}) => {
    const {data} = useGetCountries();

    return (
        <CountriesContext.Provider value={{countries: data?.countries ?? []}}>
            {children}
        </CountriesContext.Provider>
    );
};

export {CountriesProvider};
