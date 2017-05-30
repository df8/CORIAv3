package com.bigbasti.coria.parser;

import com.bigbasti.coria.graph.CoriaEdge;

import java.util.List;

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
     * Parses the Objects provided by the parser from the given data<br/>
     * @param data the data in the format described in {@link #getExpectedFormat()}
     * @return List of {@code CoriaEdge}
     */
    List<CoriaEdge> getParsedObjects(Object data) throws FormatNotSupportedException;

}
