# Created by David Fradin, 2020

from coria_lib.coria_dependencies import CORIA_METRIC_TREE, get_spl_dependent_metrics, parse_coria_metric_id
from coria_lib.coria_results import nmr_column_list_contains, input_dataframe_dict, metric_execution_timestamps
from coria_lib.coria_helper_functions import log
import sys
from time import time_ns


def execute_metrics(execution_queue, spl_table_requested_for_export=False):
    def _execute_metrics_inner(_metric_variant_id):
        _metric_keys = parse_coria_metric_id(_metric_variant_id)
        if nmr_column_list_contains(_metric_keys['metric_variant']):
            log("Skipped execution of " + _metric_variant_id + ': Metric has already been computed')

        else:
            log("Starting execution of " + _metric_variant_id)
            tstamp_start = time_ns()
            if _metric_keys['metric_algorithm'] in CORIA_METRIC_TREE and \
                    _metric_keys['metric_variant'] in CORIA_METRIC_TREE[_metric_keys['metric_algorithm']] and \
                    _metric_keys['architecture'] in CORIA_METRIC_TREE[_metric_keys['metric_algorithm']][_metric_keys['metric_variant']]:

                # CORIA_METRIC_TREE is essentially a dict with function references at its leaves, therefore we can call the function using its reference
                metric_function = CORIA_METRIC_TREE[_metric_keys['metric_algorithm']][_metric_keys['metric_variant']][_metric_keys['architecture']]

                if _metric_keys['metric_algorithm'] == "shortest-path-lengths":
                    # Compute only the metrics that were requested through command line parameters to save some execution time.
                    metric_function(input_dataframe_dict, _metric_keys['metric_variant'], spl_table_requested_for_export, get_spl_dependent_metrics(execution_queue))

                else:
                    metric_function = CORIA_METRIC_TREE[_metric_keys['metric_algorithm']][_metric_keys['metric_variant']][_metric_keys['architecture']]
                    metric_function(input_dataframe_dict, _metric_keys['metric_variant'])
            else:
                sys.exit('Unknown metric: ' + _metric_variant_id)

            metric_execution_timestamps[_metric_keys['metric_variant']] = (round(tstamp_start / 1e6), round(time_ns() / 1e6))

    for metric_variant_id in execution_queue:
        _execute_metrics_inner(metric_variant_id)
