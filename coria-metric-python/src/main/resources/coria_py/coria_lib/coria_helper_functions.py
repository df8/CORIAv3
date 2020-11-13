# Created by David Fradin, 2020

from time import gmtime, strftime


def log(*msg):
    print(strftime("%Y-%m-%d %H:%M:%S", gmtime()), *msg)


def copy_swap_columns_and_append(dataframe, swapped_columns):
    """
    Copies a dataframe, swaps the columns for node_source and node_target in the copy and re-appends it to the input dataframe.
    This way get a full dataframe with bidirectional entries instead of a sparse notation.
    For every edge A->B the resulting dataframe will have a row A->B and a now also a row B->A.
    :param dataframe:
    :param swapped_columns:
    :return:
    """

    from coria_lib.coria_config import pd

    # copy dataframe
    dataframe_temp = dataframe.copy(deep=True)

    # reorder columns
    dataframe_temp = dataframe_temp.reindex(columns=swapped_columns)

    # rename columns
    dataframe_temp.columns = list(dataframe.columns)

    # Now we have swapped the contents of the columns node_source and node_target.
    # Merge all rows into one Pandas/cuDF DataFrame:
    return pd.concat([dataframe, dataframe_temp], ignore_index=True)


def transform_networkx_result(results_temp, metric_variant_id):
    from coria_lib.coria_config import pd

    results_temp = pd.DataFrame.from_dict(results_temp, orient='index', columns=[metric_variant_id]).reset_index()
    results_temp.columns = ['node_source', metric_variant_id]
    results_temp['node_source'] = results_temp['node_source'].astype('int64')
    return results_temp
