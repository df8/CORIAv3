# Created by David Fradin, 2020

import sys

from coria_lib.coria_results import append_result_nmr


def normalise_min_max(input_dataframe_dict, metric_variant_id):
    # Min-Max normalization
    column_key_input = metric_variant_id.replace("corrected-and-normalised", "corrected").replace("normalised", "default")
    if column_key_input not in input_dataframe_dict['nmr-dependencies']:
        sys.exit('Missing data for node metric result dependency: ' + column_key_input)
    if input_dataframe_dict['nmr-dependencies'][column_key_input].max() != input_dataframe_dict['nmr-dependencies'][column_key_input].min():
        input_dataframe_dict['nmr-dependencies'][metric_variant_id] = (input_dataframe_dict['nmr-dependencies'][column_key_input] - input_dataframe_dict['nmr-dependencies'][column_key_input].min()) / (input_dataframe_dict['nmr-dependencies'][column_key_input].max() - input_dataframe_dict['nmr-dependencies'][column_key_input].min())
    else:
        input_dataframe_dict['nmr-dependencies'][metric_variant_id] = 0
    append_result_nmr(input_dataframe_dict['nmr-dependencies'], metric_variant_id, replace=True)


def normalise_max_min(input_dataframe_dict, metric_variant_id):
    # Max-Min normalization
    column_key_input = metric_variant_id.replace("corrected-and-normalised", "corrected").replace("normalised", "default")
    if column_key_input not in input_dataframe_dict['nmr-dependencies']:
        sys.exit('Missing data for node metric result dependency: ' + column_key_input)
    if input_dataframe_dict['nmr-dependencies'][column_key_input].max() != input_dataframe_dict['nmr-dependencies'][column_key_input].min():
        input_dataframe_dict['nmr-dependencies'][metric_variant_id] = (input_dataframe_dict['nmr-dependencies'][column_key_input].max() - input_dataframe_dict['nmr-dependencies'][column_key_input]) / (input_dataframe_dict['nmr-dependencies'][column_key_input].max() - input_dataframe_dict['nmr-dependencies'][column_key_input].min())
    else:
        input_dataframe_dict['nmr-dependencies'][metric_variant_id] = 1
    append_result_nmr(input_dataframe_dict['nmr-dependencies'], metric_variant_id, replace=True)
