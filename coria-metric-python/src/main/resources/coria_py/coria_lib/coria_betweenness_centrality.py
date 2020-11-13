# Created by David Fradin, 2020

from coria_lib.coria_config import USE_CUDA, pd
from coria_lib.coria_helper_functions import transform_networkx_result
from coria_lib.coria_results import append_result_nmr, append_result_emr


def betweenness_centrality(input_dataframe_dict, metric_variant_id):
    if USE_CUDA:
        from coria_lib.coria_config import cugraph
        results_temp = cugraph.betweenness_centrality(input_dataframe_dict['graph'], normalized=False)
        results_temp = results_temp[['vertex', 'betweenness_centrality']]
        results_temp.columns = ['node_source', metric_variant_id]
    else:
        from coria_lib.coria_config import nx
        results_temp = nx.betweenness_centrality(input_dataframe_dict['graph'], normalized=False)
        results_temp = transform_networkx_result(results_temp, metric_variant_id)
    append_result_nmr(results_temp, metric_variant_id)


def edge_betweenness_centrality(input_dataframe_dict, metric_variant_id):
    if USE_CUDA:
        from coria_lib.coria_config import cugraph
        results_temp = cugraph.edge_betweenness_centrality(input_dataframe_dict['graph'], normalized=False)
        results_temp.columns = ['node_source', 'node_target', metric_variant_id]
        append_result_emr(results_temp)
    else:
        from coria_lib.coria_config import nx
        results_temp = nx.edge_betweenness_centrality(input_dataframe_dict['graph'], normalized=False)
        results_temp2 = []
        for key in results_temp:
            results_temp2.append({'node_source': key[0], 'node_target': key[1], metric_variant_id: results_temp[key]})
        results_temp = pd.DataFrame(results_temp2)
        append_result_emr(results_temp)
