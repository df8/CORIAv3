package com.bigbasti.coria.parser;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;

import java.beans.Transient;
import java.util.List;
import java.util.Map;

/**
 * These parsers are used by CORIA to import and process data<br/>
 * They separate the content and create an internal grapth representation
 * before saving its result to the database
 * Created by Sebastian Gross
 */
public interface ImportModule {

    /**
     * Defines an internal unique id for this parser
     * @return id of the parser
     */
    String getIdentification();

    /**
     * Describes the short name of this import parser. e.g. CAIDA ASLinks Parser
     * @return name of parser
     */
    String getName();

    /**
     * Gives more information about the parser,  e.g. how the content of the data file
     * should look like
     * @return HTML Description of the parser
     */
    String getDescription();

    /**
     * Information about the expected content of the file and its format
     * @return Short format description
     */
    String getExpectedFormat();

    /**
     * Optional additional information which the importer needs todo his work.<br />
     * Contains the name and the type of the field<br />
     * If filled additional fields will be generated in the view and be passed to
     * the implementing object as params
     * @return Map of  <name, type> of additional parameters
     */
    Map<String, String> getAdditionalFields();

    /**
     * Parses the provided files into an internal dataset of nodes and edges
     * @param data the data in the format described in {@link #getExpectedFormat()}
     * @param params additional optional information for parser
     * @throws FormatNotSupportedException
     */
    void parseInformation(Object data, Map<String, Object> params) throws FormatNotSupportedException;

    /**
     * Returns the edges which were parsed while execution of parseInformation<br/>
     *
     * @return List of {@code CoriaEdge}
     */
    @Transient
    List<CoriaEdge> getParsedEdges();

    /**
     * Returns the nodes which were parsed while execution of parseInformation
     * @return List of {@code CoriaNode}
     */
    @Transient
    List<CoriaNode> getParsedNodes();

    /**
     * Returns the parsed DataSet (if available; usually when getImportType = DATASET)
     * @return DataSet Instance
     */
    @Transient
    DataSet getDataSet();

    /**
     * Defines what type of data is to be expected as a result from this importer
     * @return type of data which is returned by the importer
     */
    ImportType getImportType();

    enum ImportType {
        NODES,
        EDGES,
        NODES_AND_EDGES,
        DATASET
    }
}
