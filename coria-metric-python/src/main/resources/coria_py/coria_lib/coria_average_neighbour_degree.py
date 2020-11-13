# Created by David Fradin, 2020

from coria_lib.coria_helper_functions import copy_swap_columns_and_append, transform_networkx_result
from coria_lib.coria_config import pd, USE_CUDA
from coria_lib.coria_results import append_result_nmr


def pop_std(x):
    # By default, pandas applies Bessel's correction to the standard deviation formula which we revert for compatibility reasons with all other implementations of this metric.
    # The Standard Deviation Formula we use in CORIA is: sqrt( sum( x_i - µ ) / N )
    return x.std(ddof=0)


def __iterated_average_neighbour_degree(input_dataframe_dict):
    results_temp = input_dataframe_dict['shortest-path-lengths'][input_dataframe_dict['shortest-path-lengths']['distance'] == 2]
    return results_temp[['node_source', 'node_target']]


def __average_neighbour_degree_corrected(input_dataframe_dict, result_column_key, results_temp):
    results_temp = pd.merge(results_temp, input_dataframe_dict['nmr-dependencies'][['node_source', 'node-degree--default']], how='inner', left_on='node_target', right_on='node_source', sort=False)
    results_temp = results_temp.groupby('node_source_x', as_index=False).agg({'node-degree--default': ['mean', 'median', 'count', pop_std]})
    # transform 2-level column to a 1-level column
    results_temp.columns = ['--'.join(col) for col in results_temp.columns.values]
    results_temp.rename(columns={'node_source_x--': 'node_source'}, inplace=True)
    results_temp.fillna(0, inplace=True)
    results_temp[result_column_key] = results_temp['node-degree--default--mean']
    df_subset_filter = (results_temp['node-degree--default--pop_std'] != 0) & (results_temp['node-degree--default--count'] != 0)
    if df_subset_filter.any():
        results_temp[result_column_key].mask(df_subset_filter, results_temp['node-degree--default--mean'] + (((results_temp['node-degree--default--median'] - results_temp['node-degree--default--mean']) / results_temp['node-degree--default--pop_std']) / results_temp['node-degree--default--count']) * results_temp['node-degree--default--mean'], inplace=True)
    results_temp = results_temp[['node_source', result_column_key]]
    results_temp = __average_node_degree_fix_lost_rows(input_dataframe_dict, results_temp)
    return results_temp


def __join_node_degree_on_target(results_temp, input_dataframe_dict):
    results_temp = pd.merge(results_temp, input_dataframe_dict['nmr-dependencies'][['node_source', 'node-degree--default']], how='inner', left_on='node_target', right_on='node_source', sort=False)
    results_temp.rename(columns={'node_source_x': 'node_source'}, inplace=True)
    results_temp = results_temp.groupby('node_source', as_index=False).agg({'node-degree--default': ['mean']})
    results_temp.columns = results_temp.columns.get_level_values(0)
    return results_temp


def __average_node_degree_fix_lost_rows(input_dataframe_dict, results_temp):
    if results_temp.shape[0] != input_dataframe_dict['nmr-dependencies']['node-degree--default'].count():
        x = input_dataframe_dict['shortest-path-lengths'][['node_source']].drop_duplicates()
        results_temp = pd.merge(x, results_temp, how='left', on='node_source', sort=False).fillna(0)
    return results_temp


def average_neighbour_degree_default(input_dataframe_dict, metric_variant_id):
    if USE_CUDA:
        results_temp = copy_swap_columns_and_append(input_dataframe_dict['edges'], ['node_target', 'node_source'])
        results_temp = __join_node_degree_on_target(results_temp, input_dataframe_dict)
        results_temp.columns = ['node_source', metric_variant_id]
    else:
        from coria_lib.coria_config import nx
        results_temp = nx.average_neighbor_degree(input_dataframe_dict['graph'])
        results_temp = transform_networkx_result(results_temp, metric_variant_id)
    append_result_nmr(results_temp, metric_variant_id)


def average_neighbour_degree_corrected(input_dataframe_dict, metric_variant_id):
    results_temp = copy_swap_columns_and_append(input_dataframe_dict['edges'], ['node_target', 'node_source'])
    results_temp = __average_neighbour_degree_corrected(input_dataframe_dict, metric_variant_id, results_temp)
    append_result_nmr(results_temp, metric_variant_id)


def iterated_average_neighbour_degree_default(input_dataframe_dict, metric_variant_id):
    results_temp = __iterated_average_neighbour_degree(input_dataframe_dict)
    results_temp = __join_node_degree_on_target(results_temp, input_dataframe_dict)
    results_temp.columns = ['node_source', metric_variant_id]
    results_temp = __average_node_degree_fix_lost_rows(input_dataframe_dict, results_temp)
    append_result_nmr(results_temp, metric_variant_id)


def iterated_average_neighbour_degree_corrected(input_dataframe_dict, metric_variant_id):
    results_temp = __iterated_average_neighbour_degree(input_dataframe_dict)
    results_temp = __average_neighbour_degree_corrected(input_dataframe_dict, metric_variant_id, results_temp)
    append_result_nmr(results_temp, metric_variant_id)
