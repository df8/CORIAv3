# Created by David Fradin, 2020

import argparse
import os

from coria_lib.coria_helper_functions import log, copy_swap_columns_and_append

output_paths = {}
metric_parameters_dict = {}


def create_args_parser():
    parser = argparse.ArgumentParser(description="CORIA Python Metric Package")
    parser.add_argument("-m", "--metric", required=True, action="append", dest="metric", default=[], help="A list of names of the metric to execute")
    parser.add_argument("-p", "--parameter", required=False, action="append", dest="param", default=[], help="A list of argument parameters for the metrics")
    parser.add_argument("-i", "--input", required=True, action="append", dest="input", default=[], help="A list of file paths to input files for the selected metric. These files may be an edge list, a list of all shortest-path distances or a table of other metric results with one node per row.")
    parser.add_argument("-o", "--output", required=True, action="append", dest="output", default=[], help="A list of file paths to write comma-separated tables with nodes and metric results to.")
    return parser


def parse_input_args(options):
    global output_paths
    global metric_parameters_dict

    from coria_lib.coria_config import pd, USE_CUDA

    log("starting import of data...")
    input_dataframe_dict = {}
    for filepath in options.input:
        if not os.path.isfile(filepath):
            exit("Input file was not found.")
        else:
            key = filepath[filepath.find("___") + 3: filepath.find(".csv")]

            if key.startswith('nmr-dependencies-for-'):

                input_dataframe_dict['nmr-dependencies'] = pd.read_csv(filepath, delimiter='\t')
                input_dataframe_dict['nmr-dependencies'].rename(columns={'node': 'node_source'}, inplace=True)

            elif key == 'shortest-path-lengths':

                input_dataframe_dict['shortest-path-lengths'] = pd.read_csv(filepath, delimiter='\t', names=['node_source', 'node_target', 'distance'])

                input_dataframe_dict['shortest-path-lengths'] = copy_swap_columns_and_append(input_dataframe_dict['shortest-path-lengths'], ['node_target', 'node_source', 'distance'])

            elif key == 'edges':
                input_dataframe_dict['edges'] = pd.read_csv(filepath, delimiter='\t', names=['node_source', 'node_target'])
                if USE_CUDA:
                    from coria_lib.coria_config import cugraph
                    input_dataframe_dict['graph'] = cugraph.Graph()
                    input_dataframe_dict['graph'].from_cudf_edgelist(input_dataframe_dict['edges'], source='node_source', destination='node_target')

                else:
                    from coria_lib.coria_config import nx
                    input_dataframe_dict['graph'] = nx.read_edgelist(filepath)

            else:
                exit("Unsupported input file provided: " + filepath)

    # an empty dictionary will evaluate to False in Python
    if not input_dataframe_dict:
        exit("No input files provided.")

    # Organize the output paths by the algorithm type which is the key specified in the end of the passed output paths after three underscores
    for filepath in options.output:
        key = filepath[filepath.find("___") + 3: filepath.find(".csv")]
        output_paths[key] = filepath

    if not output_paths:
        exit("No input files provided.")

    for p in options.param:
        temp = p.split(':')
        if temp[0] not in metric_parameters_dict:
            metric_parameters_dict[temp[0]] = {}
        if temp[1] not in metric_parameters_dict[temp[0]]:
            metric_parameters_dict[temp[0]][temp[1]] = temp[2]

    log("import finished")

    if 'shortest-path-lengths' in input_dataframe_dict:
        log('The imported ShortestPathLengths table has the following number of (rows, cols): ', input_dataframe_dict['shortest-path-lengths'].shape)

    if 'graph' in input_dataframe_dict:
        log('The imported graph has the following number of nodes', input_dataframe_dict['graph'].number_of_nodes())
        log('The imported graph has the following number of edges', input_dataframe_dict['graph'].number_of_edges())

    if 'edges' in input_dataframe_dict:
        log('The imported Edges-table has the following number of (rows, cols): ', input_dataframe_dict['edges'].shape)

    if 'nmr-dependencies' in input_dataframe_dict:
        log('The imported NodeMetricResults-table has the following number of (rows, cols): ', input_dataframe_dict['nmr-dependencies'].shape)

    return input_dataframe_dict


def get_metric_parameters(metric_variant_id):
    global metric_parameters_dict
    if metric_variant_id in metric_parameters_dict:
        return metric_parameters_dict[metric_variant_id]
    else:
        return {}


def set_metric_parameters(metric_variant_id, key, value):
    global metric_parameters_dict
    if not metric_parameters_dict:
        metric_parameters_dict = {}
    if metric_variant_id not in metric_parameters_dict:
        metric_parameters_dict[metric_variant_id] = {}
    if key not in metric_parameters_dict[metric_variant_id]:
        metric_parameters_dict[metric_variant_id][key] = value
