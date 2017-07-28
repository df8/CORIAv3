package com.bigbasti.coria.aslinks;

import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.parser.FormatNotSupportedException;
import com.bigbasti.coria.test.BaseTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Sebastian Gross
 */
public class ASLinksEdgeImporterTest extends BaseTest {
    @Test
    public void succesfull_parse_correct_file() throws Exception, FormatNotSupportedException {
        ASLinksEdgeImporter sut = new ASLinksEdgeImporter();

        String fileContent = readResource("caida_aslinks.txt");
        sut.parseInformation(fileContent, null);
        List<CoriaEdge> edges = sut.getParsedEdges();

        assertEquals(3013, edges.size());
    }

    @Test
    public void succesfull_parse_no_header_file() throws Exception, FormatNotSupportedException {
        ASLinksEdgeImporter sut = new ASLinksEdgeImporter();

        String fileContent = readResource("caida_aslinks_no_header.txt");
        sut.parseInformation(fileContent, null);
        List<CoriaEdge> edges = sut.getParsedEdges();

        assertEquals(3013, edges.size());
    }

    @Test
    public void succesfull_parse_invalid_lines_file() throws Exception, FormatNotSupportedException {
        ASLinksEdgeImporter sut = new ASLinksEdgeImporter();

        String fileContent = readResource("caida_aslinks_invalid_lines.txt");
        sut.parseInformation(fileContent, null);
        List<CoriaEdge> edges = sut.getParsedEdges();

        assertEquals(3009, edges.size());
    }

    @Test(expected = FormatNotSupportedException.class)
    public void not_supported_format_throws_error() throws Exception, FormatNotSupportedException {
        ASLinksEdgeImporter sut = new ASLinksEdgeImporter();

        Double fileContent = 0.2;
        sut.parseInformation(fileContent, null);
    }

}