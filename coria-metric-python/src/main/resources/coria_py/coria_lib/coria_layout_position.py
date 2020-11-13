# Created by David Fradin, 2020

from coria_lib.coria_config import nx, pd
from coria_lib.coria_results import append_result_lp


def __layout_store_results(results_temp, metric_variant_id):
    results_temp = pd.DataFrame.from_dict(results_temp, orient='index', columns=['x', 'y']).reset_index()
    results_temp.columns = ['node_source', metric_variant_id + '-x', metric_variant_id + '-y']
    append_result_lp(results_temp)


def circular_layout(input_dataframe_dict, metric_variant_id):
    __layout_store_results(nx.circular_layout(input_dataframe_dict['graph']), metric_variant_id)


def spring_layout(input_dataframe_dict, metric_variant_id):
    __layout_store_results(nx.spring_layout(input_dataframe_dict['graph']), metric_variant_id)


def spectral_layout(input_dataframe_dict, metric_variant_id):
    __layout_store_results(nx.spectral_layout(input_dataframe_dict['graph']), metric_variant_id)


def random_layout(input_dataframe_dict, metric_variant_id):
    __layout_store_results(nx.random_layout(input_dataframe_dict['graph']), metric_variant_id)


def shell_layout(input_dataframe_dict, metric_variant_id):
    __layout_store_results(nx.shell_layout(input_dataframe_dict['graph']), metric_variant_id)
