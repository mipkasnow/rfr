export type Connection<T> = {
    edges: { node: T }[];
    pageInfo: {
        hasPreviousPage: boolean;
        hasNextPage: boolean;
    };
}
