# Created by David Fradin, 2020

from coria_lib.coria_config import USE_CUDA, pd
from coria_lib.coria_helper_functions import transform_networkx_result
from coria_lib.coria_results import append_result_nmr, append_result_dmr


def node_degree(input_dataframe_dict, metric_variant_id):
    if USE_CUDA:
        results_temp = input_dataframe_dict['graph'].out_degree()[['vertex', 'degree']]
        results_temp.columns = ['node_source', metric_variant_id]       
    else:
        results_temp = dict(input_dataframe_dict['graph'].degree())
        results_temp = transform_networkx_result(results_temp, metric_variant_id)
    append_result_nmr(results_temp, metric_variant_id)


def average_node_degree(input_dataframe_dict, metric_variant_id):
    if USE_CUDA:
        results_temp = {metric_variant_id: input_dataframe_dict['graph'].out_degree()['degree'].mean()}
    else:
        _sum = 0
        count = 0
        for nodeId, nodeDegree in input_dataframe_dict['graph'].degree():
            _sum = _sum + nodeDegree
            count = count + 1
        results_temp = {metric_variant_id: _sum / count}
    results_temp = pd.DataFrame(results_temp, index=[0])
    append_result_dmr(results_temp, False)
