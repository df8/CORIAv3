package com.coria.v3.caida;

/**
 * Created by Sebastian Gross, 2017
 */
public class ASLinksEdgeImporterTest /*extends BaseTest*/ {
    /* TODO /2 fix unit tests
    @Test
    public void successful_parse_correct_file() throws Exception, FormatNotSupportedException {
        ASLinksEdgeImporter sut = new ASLinksEdgeImporter();

        String fileContent = readResource("caida_aslinks.txt");
        sut.parseInformation(fileContent, null);
        Collection<EdgeEntity> edges = sut.getParsedEdges();

        assertEquals(3013, edges.size());
    }

    @Test
    public void successful_parse_no_header_file() throws Exception, FormatNotSupportedException {
        ASLinksEdgeImporter sut = new ASLinksEdgeImporter();

        String fileContent = readResource("caida_aslinks_no_header.txt");
        sut.parseInformation(fileContent, null);
        Collection<EdgeEntity> edges = sut.getParsedEdges();

        assertEquals(3013, edges.size());
    }

    @Test
    public void successful_parse_invalid_lines_file() throws Exception, FormatNotSupportedException {
        ASLinksEdgeImporter sut = new ASLinksEdgeImporter();

        String fileContent = readResource("caida_aslinks_invalid_lines.txt");
        sut.parseInformation(fileContent, null);
        Collection<EdgeEntity> edges = sut.getParsedEdges();

        assertEquals(3009, edges.size());
    }

    @Test(expected = FormatNotSupportedException.class)
    public void not_supported_format_throws_error() throws FormatNotSupportedException {
        ASLinksEdgeImporter sut = new ASLinksEdgeImporter();

        Double fileContent = 0.2;
        sut.parseInformation(fileContent, null);
    }*/

}