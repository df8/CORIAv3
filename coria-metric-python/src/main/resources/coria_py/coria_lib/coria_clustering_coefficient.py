# Created by David Fradin, 2020

from coria_lib.coria_config import USE_CUDA, pd
from coria_lib.coria_helper_functions import transform_networkx_result, copy_swap_columns_and_append
from coria_lib.coria_results import append_result_nmr


def local_clustering_coefficient_default(input_dataframe_dict, metric_variant_id):
    if USE_CUDA:
        edges = copy_swap_columns_and_append(input_dataframe_dict['edges'], ['node_target', 'node_source'])
        results_temp = pd.merge(edges, edges, how='inner', left_on='node_target', right_on='node_source', sort=False)
        results_temp.columns = ['node_source', 'node_neighbour_1hop', 'node_neighbour_1hop_copy', 'node_neighbour_2hop']
        results_temp = results_temp[(results_temp['node_source'] != results_temp['node_neighbour_1hop']) & (results_temp['node_source'] != results_temp['node_neighbour_2hop'])]
        results_temp = pd.merge(results_temp, edges, how='inner', left_on='node_neighbour_2hop', right_on='node_source', sort=False)
        results_temp.columns = ['node_source', 'node_neighbour_1hop', 'node_neighbour_1hop_copy', 'node_neighbour_2hop', 'node_neighbour_2hop_copy', 'node_neighbour_3hop']

        results_temp = results_temp[(results_temp['node_neighbour_1hop'] != results_temp['node_neighbour_3hop']) & (results_temp['node_source'] == results_temp['node_neighbour_3hop'])]
        results_temp = results_temp[['node_source', 'node_neighbour_3hop']]
        if results_temp['node_source'].count() == 0:
            results_temp = edges[['node_source']].drop_duplicates()
            results_temp[metric_variant_id] = 0
        else:
            results_temp = results_temp.groupby('node_source', as_index=False).agg(['count'])
            results_temp.columns = results_temp.columns.get_level_values(0)
            results_temp = pd.merge(results_temp, input_dataframe_dict['nmr-dependencies'][['node_source', 'node-degree--default']], how='right', on='node_source', sort=False).fillna(0)
            results_temp[metric_variant_id] = 0.0
            df_subset_filter = results_temp['node-degree--default'] > 1
            if df_subset_filter.any():
                results_temp[metric_variant_id].mask(df_subset_filter, results_temp['node_neighbour_3hop'] / (results_temp['node-degree--default'] * (results_temp['node-degree--default'] - 1.0)), inplace=True)
            results_temp = results_temp[['node_source', metric_variant_id]]        
    else:
        from coria_lib.coria_config import nx
        results_temp = nx.clustering(input_dataframe_dict['graph'])
        results_temp = transform_networkx_result(results_temp, metric_variant_id)
    append_result_nmr(results_temp, metric_variant_id)


def local_clustering_coefficient_corrected(input_dataframe_dict, metric_variant_id):
    input_dataframe_dict['nmr-dependencies'][metric_variant_id] = input_dataframe_dict['nmr-dependencies']['local-clustering-coefficients--default'] + input_dataframe_dict['nmr-dependencies']['local-clustering-coefficients--default'] * input_dataframe_dict['nmr-dependencies']['node-degree--default'] / 4
    append_result_nmr(input_dataframe_dict['nmr-dependencies'], metric_variant_id, replace=True)
