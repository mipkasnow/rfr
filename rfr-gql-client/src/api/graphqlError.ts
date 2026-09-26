import {CombinedGraphQLErrors, ErrorLike, ServerError} from "@apollo/client";

const USER_FACING_CLASSIFICATIONS = ["BAD_REQUEST", "NOT_FOUND"];
const UNAUTHORIZED_CLASSIFICATIONS = ["UNAUTHORIZED", "FORBIDDEN"];

const classifications = (error: ErrorLike | undefined): string[] =>
    CombinedGraphQLErrors.is(error)
        ? error.errors.map((e) => String(e.extensions?.classification))
        : [];

export const errorMessage = (error: ErrorLike | undefined, fallback: string): string => {
    const userFacing = CombinedGraphQLErrors.is(error)
        ? error.errors.find((e) => USER_FACING_CLASSIFICATIONS.includes(String(e.extensions?.classification)))
        : undefined;
    return userFacing?.message || fallback;
};

export const isUnauthorized = (error: ErrorLike | undefined): boolean =>
    (ServerError.is(error) && error.statusCode === 401)
    || classifications(error).some((c) => UNAUTHORIZED_CLASSIFICATIONS.includes(c));
