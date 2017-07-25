package com.bigbasti.coria.parser;

import com.bigbasti.coria.graph.CoriaEdge;

import java.util.List;
import java.util.Map;

/**
 * These parsers are used by CORIA to import and process data<br/>
 * They separate the content and create an internal grapth representation
 * before saving its result to the database
 * Created by Sebastian Gross
 */
public interface InputParser {

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
     * Parses the Objects provided by the parser from the given data<br/>
     * @param data the data in the format described in {@link #getExpectedFormat()}
     * @return List of {@code CoriaEdge}
     */
    List<CoriaEdge> getParsedObjects(Object data, Map<String, Object> params) throws FormatNotSupportedException;

}
