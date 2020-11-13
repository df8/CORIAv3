# Created by David Fradin, 2020

from coria_lib.coria_config import pd, USE_CUDA
from coria_lib.coria_results import append_result_nmr, metric_execution_timestamps


def shortest_path_length(input_dataframe_dict, metric_variant_id, spl_table_requested_for_export, spl_dependent_metrics):
    ecc_requested = 'eccentricity--default' in spl_dependent_metrics
    aspl_requested = 'average-shortest-path-length--default' in spl_dependent_metrics
    iandd_requested = 'iterated-average-neighbour-degree--default' in spl_dependent_metrics
    iandc_requested = 'iterated-average-neighbour-degree--corrected' in spl_dependent_metrics
    results_spl = []
    results_nmr_from_spl = []  # this list collects up to 4 node metric results in tuples for each row.

    from time import time_ns
    tstamp_start = time_ns()

    result_column_names = ['node_source'] + spl_dependent_metrics
    node_degree_lookup = None

    if ecc_requested or aspl_requested or iandd_requested or iandc_requested:
        node_degree_lookup = input_dataframe_dict['nmr-dependencies'][['node_source', 'node-degree--default']]

    def _shortest_path_length_inner(_df1):
        if ecc_requested or aspl_requested or iandd_requested or iandc_requested:
            nmr_row_results = [node_source]

            # ECC
            if ecc_requested:
                ecc = _df1['distance'].max()
                nmr_row_results.append(ecc)

            # ASPL
            if aspl_requested:
                aspl = _df1['distance'].mean()
                nmr_row_results.append(aspl)

            # IAND/Default
            if iandd_requested or iandc_requested:
                _results_temp = _df1[_df1['distance'] == 2][['vertex']]
                if _results_temp.empty:
                    if iandd_requested:
                        nmr_row_results.append(0)
                    if iandc_requested:
                        nmr_row_results.append(0)
                else:
                    _results_temp = pd.merge(_results_temp, node_degree_lookup, how='inner', left_on='vertex', right_on='node_source', sort=False)
                    ndeg_series = _results_temp['node-degree--default']
                    ndeg_mean = ndeg_series.mean()
                    if iandd_requested:
                        nmr_row_results.append(ndeg_mean)

                    # IAND/Corrected
                    if iandc_requested:
                        iand_corrected = ndeg_mean
                        ndeg_std = ndeg_series.std(ddof=0)
                        ndeg_count = ndeg_series.count()
                        ndeg_median = ndeg_series.median()
                        if ndeg_std != 0 and ndeg_count != 0:
                            iand_corrected = ndeg_mean + (((ndeg_median - ndeg_mean) / ndeg_std) / ndeg_count) * ndeg_mean
                        nmr_row_results.append(iand_corrected)

            results_nmr_from_spl.append(nmr_row_results)

        if spl_table_requested_for_export:
            # filter out all redundant entries. We're expecting an indirect graph, hence dist(A, B) == dist(B, A)
            _df1 = _df1[_df1['vertex'] > node_source].copy()

            # add column
            _df1['node_source'] = node_source

            # store result
            results_spl.append(_df1)

    if USE_CUDA:
        # Combination of SPL, ECC, ASPL, IAND/Default and IAND/Corrected with storing the shortest paths being optional.
        # TODO /1 Test all metrics with a disconnected node too        
        # Storing all shortest paths requires lots of GPU memory { O( 3 * |N| * (|N|-1) / 2) } therefore we will avoid it unless
        # explicitly requested by the corresponding command line parameter "-o [...]___shortestpathlength[...]"
        from coria_lib.coria_config import cugraph

        for node_source in input_dataframe_dict['graph'].nodes().values_host:
            # calculate all shortest paths starting from n
            df1 = cugraph.sssp(input_dataframe_dict['graph'], node_source)

            # remove unused column predecessor
            df1.drop('predecessor', axis=1, inplace=True)

            if not df1.empty:
                _shortest_path_length_inner(df1)

    else:
        from coria_lib.coria_config import nx
        from numpy import int64  # casts string to long

        spl = nx.shortest_path(input_dataframe_dict['graph'])
        for _node_source in spl:
            node_source = int64(_node_source)
            df1 = []
            for _node_target in spl[_node_source]:
                df1.append({'vertex': int64(_node_target), 'distance': len(spl[_node_source][_node_target]) - 1})
            df1 = pd.DataFrame(df1)
            if not df1.empty:
                _shortest_path_length_inner(df1)

                # Store the node metric results
    if len(results_nmr_from_spl) > 0:
        results_temp = pd.DataFrame(results_nmr_from_spl, columns=result_column_names)
        append_result_nmr(results_temp, result_column_names[1:])
        for variant_id in result_column_names[1:]:
            metric_execution_timestamps[variant_id] = (round(tstamp_start / 1e6), round(time_ns() / 1e6))

    # Store the shortest path lengths
    if spl_table_requested_for_export:
        # merge all dataframes into one, resulting in |V|*(|V|-1)/2 rows
        results_spl = pd.concat(results_spl)

        # rename columns
        results_spl.rename(columns={'vertex': 'node_target'}, inplace=True)

        # reorder columns
        results_spl = results_spl.reindex(columns=['node_source', 'node_target', 'distance'])

        # reset row index
        results_spl.reset_index(drop=True, inplace=True)

        # write into input structure
        input_dataframe_dict['shortest-path-lengths'] = results_spl
