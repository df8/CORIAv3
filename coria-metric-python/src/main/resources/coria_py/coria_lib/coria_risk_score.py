# Created by David Fradin, 2020

from coria_lib.coria_parse_args import get_metric_parameters
from coria_lib.coria_results import append_result_nmr


def unified_risk_score(input_dataframe_dict, metric_variant_id):
    input_dataframe_dict['nmr-dependencies'][metric_variant_id] = \
        0.25 * input_dataframe_dict['nmr-dependencies']['node-degree--normalised'] + \
        0.15 * input_dataframe_dict['nmr-dependencies']['average-neighbour-degree--corrected-and-normalised'] + \
        0.1 * input_dataframe_dict['nmr-dependencies']['iterated-average-neighbour-degree--corrected-and-normalised'] + \
        0.25 * input_dataframe_dict['nmr-dependencies']['betweenness-centrality--normalised'] + \
        0.125 * input_dataframe_dict['nmr-dependencies']['eccentricity--normalised'] + \
        0.125 * input_dataframe_dict['nmr-dependencies']['average-shortest-path-length--normalised']

    append_result_nmr(input_dataframe_dict['nmr-dependencies'], metric_variant_id, replace=True)


def connectivity_risk_classification(input_dataframe_dict, metric_variant_id):
    parameters_dict = get_metric_parameters(metric_variant_id)
    if 'threshold-low' not in parameters_dict:
        parameters_dict['threshold-low'] = 0.45
    else:
        parameters_dict['threshold-low'] = float(parameters_dict['threshold-low'])

    if 'threshold-high' not in parameters_dict:
        parameters_dict['threshold-high'] = 0.55
    else:
        parameters_dict['threshold-high'] = float(parameters_dict['threshold-high'])

    lcc_mask = (input_dataframe_dict['nmr-dependencies']['local-clustering-coefficients--corrected-and-normalised'] >= 0.25).astype(float) * 0.25
    # lcc_mask now contains either 0 or 0.25.

    input_dataframe_dict['nmr-dependencies'][metric_variant_id] = 0

    # if URS + 0.25 * MASK < LowerThreshold then -1
    input_dataframe_dict['nmr-dependencies'][metric_variant_id].mask(input_dataframe_dict['nmr-dependencies']['unified-risk-score--default'] + lcc_mask < parameters_dict['threshold-low'], -1, inplace=True)

    # if URS - 0.25 * MASK >= HigherThreshold then 1
    input_dataframe_dict['nmr-dependencies'][metric_variant_id].mask(input_dataframe_dict['nmr-dependencies']['unified-risk-score--default'] - lcc_mask >= parameters_dict['threshold-high'], 1, inplace=True)

    append_result_nmr(input_dataframe_dict['nmr-dependencies'], metric_variant_id, replace=True)
