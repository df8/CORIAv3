/**
 * Created by David Fradin, 2020
 */

import {ApolloClient} from 'apollo-client';
import {InMemoryCache} from 'apollo-cache-inmemory';
import {onError} from 'apollo-link-error';
import {ApolloLink} from 'apollo-link';
import buildGraphQLProvider, {buildQuery} from 'ra-data-graphql-simple';
import {CREATE, GET_ONE, GET_MANY_REFERENCE, GET_LIST} from 'ra-core';
import {createUploadLink} from "apollo-upload-client";
import gql from 'graphql-tag';

const connectionURI = `http://${window.location.hostname}:8080/graphql`;

const coriaDataProviderExt = (fetchType, resource, params, defaultDataProvider) => {
    if (fetchType === CREATE && params.data.files) {
        //Special treatment for file upload: Reduce object depth
        const files = params.data.files.map(file => file.rawFile);
        return defaultDataProvider(fetchType, resource, {
            ...params,
            data: {
                ...params.data,
                files
            }
        });
    } else if (resource === 'Metric' && fetchType === CREATE) {
        const varKeys = Object.keys(params.data);
        // Check whether we have any metric parameters such as for Connectivity Risk Classification and
        // package those into a dedicated array called parameters
        if (varKeys.length > 4) {
            const KNOWN_KEYS = ['datasetId', 'metricAlgorithm', 'metricAlgorithmVariant', 'metricAlgorithmImplementation'];
            params.data['parameters'] = [];
            for (let i in varKeys) {
                if (!KNOWN_KEYS.includes(varKeys[i])) {
                    params.data['parameters'].push({
                        'key': varKeys[i],
                        'value': params.data[varKeys[i]]
                    });
                    delete params.data[varKeys[i]]
                }
            }
        }
    }
    return defaultDataProvider(fetchType, resource, params);
}

const coriaBuildQuery = introspection => (fetchType, resource, params) => {
    const builtQuery = buildQuery(introspection)(fetchType, resource, params);
    console.log(fetchType, resource, params);
    if (resource === 'Node' && (fetchType === GET_MANY_REFERENCE || fetchType === GET_LIST)) {
        return {
            ...builtQuery,// Use the default query variables and parseResponse
            // Override the query
            query: gql`
                query allNodes($page: Int, $perPage: Int, $sortField: String, $sortOrder: String, $filter: NodeFilter) {
                    items: allNodes(page: $page, perPage: $perPage, sortField: $sortField, sortOrder: $sortOrder, filter: $filter) {
                        id
                        name
                        metricResults {
                            metric {
                                id
                                metricAlgorithmImplementation {
                                    id
                                    metricAlgorithmVariant {
                                        id
                                        metricAlgorithm {
                                            id
                                            __typename
                                        }
                                        __typename
                                    }
                                    __typename
                                }
                                __typename
                            }
                            value
                            __typename
                        }
                        attributesList {
                            key
                            value
                            __typename
                        }
                        layoutPositions {
                            metric {
                                id
                                metricAlgorithmImplementation {
                                    id
                                    metricAlgorithmVariant {
                                        id
                                        metricAlgorithm {
                                            id
                                            __typename
                                        }
                                        __typename
                                    }
                                    __typename
                                }
                            }
                            x
                            y
                        }
                        __typename
                    }
                    total: _allNodesMeta(page: $page, perPage: $perPage, filter: $filter) {
                        count
                        __typename
                    }
                }`,
        };
    } else if (resource === 'Edge' && (fetchType === GET_MANY_REFERENCE || fetchType === GET_LIST)) {
        return {
            ...builtQuery,// Use the default query variables and parseResponse
            // Override the query
            query: gql`
                query allEdges($page: Int, $perPage: Int, $sortField: String, $sortOrder: String, $filter: EdgeFilter) {
                    items: allEdges(page: $page, perPage: $perPage, sortField: $sortField, sortOrder: $sortOrder, filter: $filter) {
                        id
                        name
                        metricResults {
                            metric {
                                id
                                metricAlgorithmImplementation {
                                    id
                                    metricAlgorithmVariant {
                                        id
                                        metricAlgorithm {
                                            id
                                            __typename
                                        }
                                        __typename
                                    }
                                    __typename
                                }
                                __typename
                            }
                            value
                            __typename
                        }
                        attributesList {
                            key
                            value
                            __typename
                        }
                        locations {
                            location {
                                id
                                continent
                                country
                                region
                                city
                                latitude
                                longitude
                                __typename
                            }
                            source
                            __typename
                        }
                        __typename
                    }
                    total: _allEdgesMeta(page: $page, perPage: $perPage, filter: $filter) {
                        count
                        __typename
                    }
                }`,
        };
    } else if (resource === 'Dataset' && fetchType === GET_ONE) {
        return {
            ...builtQuery,// Use the default query variables and parseResponse
            // Override the query
            query: gql`query Dataset($id: ID!) {
                data: Dataset(id: $id) {
                    id
                    name
                    created
                    metrics {
                        id
                        __typename
                    }
                    attributesList {
                        key
                        value
                        __typename
                    }
                    metricResults {
                        metric {
                            id
                            metricAlgorithmImplementation {
                                id
                                metricAlgorithmVariant {
                                    id
                                    metricAlgorithm {
                                        id
                                        __typename
                                    }
                                    __typename
                                }
                                __typename
                            }
                            __typename
                        }
                        value
                        __typename
                    }
                    __typename
                }
            }`
        }
    } else if (resource === 'MetricAlgorithm' && fetchType === GET_LIST) {
        return {
            ...builtQuery,// Use the default query variables and parseResponse
            // Override the query
            query: gql`
                query allMetricAlgorithms($page: Int, $perPage: Int, $sortField: String, $sortOrder: String, $filter: MetricAlgorithmFilter) {
                    items: allMetricAlgorithms(page: $page, perPage: $perPage, sortField: $sortField, sortOrder: $sortOrder, filter: $filter) {
                        id
                        name
                        description
                        type
                        metricAlgorithmVariants {
                            id
                            name
                            description
                            parameters {
                                id
                                index
                                description
                                defaultValue
                                isRequired
                                type
                                __typename
                            }
                            dependencies {
                                id
                                name
                                metricAlgorithm {
                                    id
                                    name
                                    __typename
                                }
                                __typename
                            }
                            implementations {
                                id
                                provider
                                technology
                                description
                                speedIndex
                                available
                                unavailableReason
                                __typename
                            }
                            __typename
                        }
                        __typename
                    }
                    total: _allMetricAlgorithmsMeta(page: $page, perPage: $perPage, filter: $filter) {
                        count
                        __typename
                    }
                }`,
        };
    } else if (resource === 'ShortestPathLength' && fetchType === GET_LIST) {
        return {
            ...builtQuery,// Use the default query variables and parseResponse
            // Override the query
            query: gql`
                query allShortestPathLengths($page: Int, $perPage: Int, $sortField: String, $sortOrder: String, $filter: ShortestPathLengthFilter) {
                    items: allShortestPathLengths(page: $page, perPage: $perPage, sortField: $sortField, sortOrder: $sortOrder, filter: $filter) {
                        id
                        metric {
                            id
                            metricAlgorithmImplementation {
                                id
                                metricAlgorithmVariant {
                                    id
                                    metricAlgorithm {
                                        id
                                        __typename
                                    }
                                    __typename
                                }
                                __typename
                            }
                            __typename
                        }
                        nodeSourceName
                        nodeTargetName
                        distance
                        __typename
                    }
                    total: _allShortestPathLengthsMeta(page: $page, perPage: $perPage, filter: $filter) {
                        count
                        __typename
                    }
                }`,
        };
    } else if (resource === 'Metric' && fetchType === GET_MANY_REFERENCE) {
        return {
            ...builtQuery,// Use the default query variables and parseResponse
            // Override the query
            query: gql`
                query allMetrics($page: Int, $perPage: Int, $sortField: String, $sortOrder: String, $filter: MetricFilter) {
                    items: allMetrics(page: $page, perPage: $perPage, sortField: $sortField, sortOrder: $sortOrder, filter: $filter) {
                        id
                        metricAlgorithmImplementation {
                            id
                            __typename
                        }
                        status
                        started
                        finished
                        message
                    }
                    total: _allMetricsMeta(page: $page, perPage: $perPage, filter: $filter) {
                        count
                        __typename
                    }
                }`,
        };
    }

    return builtQuery;
};

export default function buildDataProvider() {

    const client = new ApolloClient({
        link: ApolloLink.from([
            onError(({graphQLErrors, networkError}) => {
                if (graphQLErrors)
                    graphQLErrors.forEach(({message, locations, path}) =>
                        console.log(
                            `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}`,
                        ),
                    );
                if (networkError) console.log(`[Network error]: ${networkError}`);
            }),
            createUploadLink({
                uri: connectionURI
            })
        ]),
        cache: new InMemoryCache()
    });
    return buildGraphQLProvider({buildQuery: coriaBuildQuery, client: client})
        .then(function (defaultDataProvider) {
            //return defaultDataProvider;
            return function (fetchType, resource, params) {
                return coriaDataProviderExt(fetchType, resource, params, defaultDataProvider);
            }
        });
}

/**
 * Source: https://stackoverflow.com/questions/55266746/react-admin-cannot-upload-a-file-using-fileinput
 * @param file
 * @returns {Promise<unknown>}
 */
/*const convertFileToBase64 = file => new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file.rawFile);

    reader.onload = () => resolve(reader.result);
    reader.onerror = reject;
});*/