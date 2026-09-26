import {gql} from "@apollo/client";
import {useQuery} from "@apollo/client/react";
import {useState} from "react";
import {Connection} from "../types/Connection";
import {Photo} from "../types/Photo";
import {Stat} from "../types/Stat";

export const FEED_PAGE_SIZE = 12;

type GetFeedData = {
    feed: {
        photos: Connection<Photo>;
        stat: Stat[];
    };
};

export const GET_FEED = gql(`
    query GetFeed($page: Int, $size: Int, $withFriends: Boolean!, $country: String) {
        feed(withFriends: $withFriends) {
            photos(page: $page, size: $size, country: $country) {
                edges {
                    node {
                        id
                        src
                        country {
                            code
                            name
                            flag
                        }
                        description
                        isOwner
                        likes {
                            total
                            likes {
                                user
                            }
                        }
                    }
                }
                pageInfo {
                    hasPreviousPage
                    hasNextPage
                }
            }
            stat {
                count
                country {
                    code
                }
            }
        }
    }
`);

type getFeedRequestType = {
    withFriends: boolean,
    country?: string | null,
}

export const useGetFeed = (req: getFeedRequestType) => {
    const [loadingMore, setLoadingMore] = useState(false);
    const {data: currentData, previousData, loading, error, refetch, fetchMore} = useQuery<GetFeedData>(GET_FEED, {
        variables: {
            withFriends: req.withFriends,
            page: 0,
            size: FEED_PAGE_SIZE,
            country: req.country ?? null,
        },
        fetchPolicy: "cache-and-network",
    });

    const data = currentData ?? previousData;
    const photos = data?.feed?.photos?.edges?.map((e) => e.node) ?? [];
    const hasNextPage = data?.feed?.photos?.pageInfo?.hasNextPage ?? false;

    const loadMore = async () => {
        if (loadingMore || !hasNextPage || !currentData) {
            return;
        }
        setLoadingMore(true);
        try {
            await fetchMore({
                variables: {
                    page: Math.ceil(photos.length / FEED_PAGE_SIZE),
                },
                updateQuery: (previous, {fetchMoreResult}) => ({
                    feed: {
                        ...previous.feed,
                        photos: {
                            ...fetchMoreResult.feed.photos,
                            edges: [...previous.feed.photos.edges, ...fetchMoreResult.feed.photos.edges],
                        },
                    },
                }),
            });
        } finally {
            setLoadingMore(false);
        }
    };

    return {
        photos,
        stat: data?.feed?.stat ?? [],
        hasNextPage,
        initialLoading: loading && !data,
        refreshing: loading && !currentData && Boolean(previousData),
        loadingMore,
        loadMore,
        error,
        refetch,
    };
}
