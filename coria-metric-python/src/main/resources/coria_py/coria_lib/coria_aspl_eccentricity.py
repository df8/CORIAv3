# Created by David Fradin, 2020

from coria_lib.coria_results import append_result_nmr, append_result_dmr


def __average_shortest_path_length_postprocessing(results_temp, metric_variant_id):
    results_temp = results_temp[['node_source', metric_variant_id]]
    results_temp.columns = results_temp.columns.get_level_values(0)
    append_result_nmr(results_temp, metric_variant_id)


def average_shortest_path_length(input_dataframe_dict, metric_variant_id):
    results_temp = input_dataframe_dict['shortest-path-lengths'].groupby('node_source', as_index=False).agg({'distance': ['sum', 'count']})
    results_temp[metric_variant_id] = results_temp['distance']['sum'] / (results_temp['distance']['count'] + 1)
    __average_shortest_path_length_postprocessing(results_temp, metric_variant_id)


def eccentricity(input_dataframe_dict, metric_variant_id):
    results_temp = input_dataframe_dict['shortest-path-lengths'].groupby('node_source', as_index=False).agg({'distance': ['max']})
    results_temp[metric_variant_id] = results_temp['distance']['max']
    __average_shortest_path_length_postprocessing(results_temp, metric_variant_id)


def graph_diameter(input_dataframe_dict, metric_variant_id):
    from coria_lib.coria_config import pd, nx
    results_temp = {metric_variant_id: nx.diameter(input_dataframe_dict['graph'])}
    results_temp = pd.DataFrame(results_temp, index=[0])
    append_result_dmr(results_temp, False)
