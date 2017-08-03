package com.bigbasti.coria.export;

import com.bigbasti.coria.dataset.DataSet;

import java.util.Map;

public interface ExportAdapter {
    /**
     * Defines an internal unique id for this exporter
     * @return id of the exporter
     */
    String getIdentification();

    /**
     * Describes the short name of this exporter. e.g. CORIA Dataset Export Adapter
     * @return name of exporter
     */
    String getName();

    /**
     * Gives more information about the exporter,  e.g. how the content of exportet file will look like
     * @return HTML Description of the exporter
     */
    String getDescription();

    /**
     * Optional additional information which the exporter needs to do his work.<br />
     * Contains the name and the type of the field<br />
     * If filled additional fields will be generated in the view and be passed to
     * the implementing object as params
     * @return Map of  <name, type> of additional parameters
     */
    Map<String, String> getAdditionalFields();

    /**
     * Performs the export operation on the given dataset and returns the object containing the exportet Data
     * @param dataset the dataset to be exportet
     * @param params additional params for the export adapter
     * @return {@link DataSet}
     */
    Object exportDataSet(DataSet dataset, Map<String, Object> params);
}
