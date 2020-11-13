# Created by David Fradin, 2020

import sys
from coria_lib.coria_config import pd
from coria_lib.coria_helper_functions import log

input_dataframe_dict = {}
spl_to_dict = False
result_column_keys_nmr = ['node_source']
results_emr = None
results_dmr = None
results_lp = None
results_temp = None
metric_execution_timestamps = {}


def set_input_dataframe_dict(val):
    global input_dataframe_dict
    input_dataframe_dict = val


def append_result_nmr(df, metric_variant_id, replace=False):
    global input_dataframe_dict
    global result_column_keys_nmr

    if replace or 'nmr-dependencies' not in input_dataframe_dict or input_dataframe_dict['nmr-dependencies'] is None:
        input_dataframe_dict['nmr-dependencies'] = df
    else:
        input_dataframe_dict['nmr-dependencies'] = pd.merge(input_dataframe_dict['nmr-dependencies'], df, how='left', left_on='node_source', right_on='node_source', sort=False)

    if isinstance(metric_variant_id, list):
        result_column_keys_nmr.extend(metric_variant_id)
    else:
        result_column_keys_nmr.append(metric_variant_id)


def nmr_column_list_contains(metric_variant_id):
    global result_column_keys_nmr
    return metric_variant_id in result_column_keys_nmr


def reset_results():
    global result_column_keys_nmr
    global input_dataframe_dict
    input_dataframe_dict['nmr-dependencies'] = None
    result_column_keys_nmr = ['node_source']


def append_result_emr(df):
    global input_dataframe_dict
    global results_emr

    if results_emr is None:
        results_emr = df
    else:
        results_emr = pd.merge(results_emr, df, how='left', on=['node_source', 'node_target'], sort=False)


def append_result_lp(df):
    global results_lp

    if results_lp is None:
        results_lp = df
    else:
        results_lp = pd.merge(results_lp, df, how='left', on=['node_source'], sort=False)


def append_result_dmr(df, replace=False):
    global results_dmr

    if replace or results_dmr is None:
        results_dmr = df
    else:
        results_dmr = pd.merge(results_dmr, df, how='inner', left_index=True, right_index=True, sort=False)


def write_output_to_files(output_paths=None):
    if output_paths is None:
        from coria_lib.coria_parse_args import output_paths
    # Library-specific writers
    handled_export = False
    if 'shortest-path-lengths' in input_dataframe_dict and 'shortestpathlength' in output_paths and getattr(input_dataframe_dict['shortest-path-lengths'], "to_csv", None) is not None:
        # df.to_csv() works for Pandas dataframes and cuDF dataframes with the same function call.
        input_dataframe_dict['shortest-path-lengths'].to_csv(output_paths['shortestpathlength'], sep=',', index=False, header=True)
        handled_export = True

    if results_emr is not None and 'edge' in output_paths and getattr(results_emr, "to_csv", None) is not None:
        results_emr.to_csv(output_paths['edge'], sep=',', index=False, header=True)
        handled_export = True

    if 'nmr-dependencies' in input_dataframe_dict and 'node' in output_paths and getattr(input_dataframe_dict['nmr-dependencies'], "to_csv", None) is not None:
        input_dataframe_dict['nmr-dependencies'][result_column_keys_nmr].to_csv(output_paths['node'], sep=',', index=False, header=True)
        handled_export = True

    if results_lp is not None and getattr(results_lp, "to_csv", None) is not None:
        results_lp.to_csv(output_paths['layoutposition'], sep=',', index=False, header=True)
        handled_export = True

    if 'executiontimestamps' in output_paths:
        with open(output_paths['executiontimestamps'], "w") as outputFile:
            for key, value in metric_execution_timestamps.items():
                outputFile.write(key + "," + str(value[0]) + "," + str(value[1]) + "," + str(value[1] - value[0]) + "\n")

    if results_dmr is not None and 'dataset' in output_paths and getattr(results_dmr, "to_csv", None) is not None:
        results_dmr.to_csv(output_paths['dataset'], sep=',', index=False, header=True)
        handled_export = True

    if not handled_export and results_temp is not None:
        with open(output_paths['dataset'], "w") as outputFile:
            if isinstance(results_temp, (str, float, int)):
                outputFile.write(str(results_temp))
            elif isinstance(results_temp, dict):
                # if spl_to_dict and metric_id.startswith("shortest-path-lengths--default--python3"):
                #    import json
                #    outputFile.write(json.dumps(results, separators=(',', ':')).replace('"', ''))
                # else:
                for key, value in results_temp.items():
                    if isinstance(key, tuple):
                        outputFile.write(','.join(map(str, key)) + "," + str(value) + "\n")
                    else:
                        import numpy as np

                        if isinstance(value, np.ndarray):
                            outputFile.write(key)
                            for value2 in value:
                                outputFile.write("," + str(value2))
                            outputFile.write("\n")
                        else:
                            outputFile.write(key + "," + str(value) + "\n")
            elif isinstance(results_temp, list):
                for row in results_temp:
                    outputFile.write(','.join(map(str, row)) + "\n")
            else:
                sys.exit('Unknown export type: ' + str(type(results_temp)))

    log("Finished writing to file and finished execution")

