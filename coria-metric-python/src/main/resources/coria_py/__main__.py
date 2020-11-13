# Created by David Fradin, 2020

if __name__ == "__main__":
    from coria_lib.coria_config import set_global_settings, coria_imports
    from coria_lib.coria_parse_args import create_args_parser

    options = create_args_parser().parse_args()

    set_global_settings(use_cuda=options.metric[0].endswith("python3-c-cuda--rapids-cugraph"))

    # CORIA components
    coria_imports()

    from coria_lib.coria_helper_functions import log
    from coria_lib.coria_parse_args import parse_input_args
    from coria_lib.coria_results import set_input_dataframe_dict

    set_input_dataframe_dict(parse_input_args(options))
    from coria_lib.coria_execute import execute_metrics
    from coria_lib.coria_results import write_output_to_files
    from coria_lib.coria_parse_args import output_paths

    execute_metrics(options.metric, spl_table_requested_for_export='shortestpathlength' in output_paths)

    log("finished computing")
    write_output_to_files()
