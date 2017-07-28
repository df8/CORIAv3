package com.bigbasti.coria.aslinks;

import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.parser.FormatNotSupportedException;
import com.bigbasti.coria.parser.InputParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for ASLink Files provided by CAIDA
 * After parsing it tries to resolve the AS numbers to domain names using
 * the provided resolution file
 * It also tries to match the as number to the provided geo location file
 * to resolve the country, city and geo coordinates of the as
 * Created by Sebastian Gross
 */
@Component
public class ASLinksEdgeResolveGeoImporter implements InputParser {

    private Logger logger = LoggerFactory.getLogger(ASLinksEdgeResolveGeoImporter.class);

    ArrayList<CoriaEdge> importedEdges = new ArrayList<>();
    ArrayList<CoriaNode> importedNodes = new ArrayList<>();

    @Override
    public String getIdentification() {
        return "caida-as-links-resolve-geo-parser";
    }

    @Override
    public String getName() {
        return "CAIDA AS-Links Resolve Geo Parser";
    }

    @Override
    public String getDescription() {
        return "<p>This parser tries to resolve the name of an AS and its GEO attributes (City, Country and coordinates)</p>" +
                "<p>All uploaded files must match the format described below</p>\n" +
                "<p>Each line represents one link, lines starting with <code>#</code> are ignored! Only lines are processed which begin with <code>I</code> or <code>D</code></p>\n" +
                "<p>One example line could look like this: <code>I\t10010\t23775\t2\t9\t12\t15\t28\t29</code> where the parameters are separated by <code>tab</code> characters and represent the following information (in the same order) <code>I   from_AS   to_AS   gap_length   monitor_key1   monitor_key2 ...</code></p>\n" +
                "<p>compatible files can be obtained from caida.org - for example here: <a href=\"http://data.caida.org/datasets/topology/ark/ipv4/as-links/team-1/2017/\" target=\"blank\">http://data.caida.org/datasets/topology/ark/ipv4/as-links/team-1/2017/</a></p>\n" +
                "<p>The expected file format is <code>.txt</code> - so please extract the <code>.gz</code> file from caida.org before uploading</p><hr>" +
                "<p><strong>Organizations Information</strong></p>" +
                "<p>Please select the file containing a mapping of AS-Number to Name for the uploaded dataset<p/>" +
                "<p>The format of the file must match these parameters: <code>AS-Number|changed|AS-Name|org_id|source</code></p>" +
                "<p>An example of valid data would be <code>282|20090426|MERIT-AUX-AS|MERITT-ARIN|ARIN</code></p>" +
                "<p>All other lines (not matching this format) will be ignored, <u>make sure to remove unneeded lines by yourself to help speedup the processing of the file</u></p>" +
                "<p>You can obtain these files from <a href=\"http://data.caida.org/datasets/as-organizations/\" target=\"_blank\">caida.org</a></p>" +
                "<p>The expected file format is <code>.txt</code> - so please extract the <code>.gz</code> file from caida.org before uploading</p><hr/>" +
                "<p><strong>Locations Information</strong></p>" +
                "<p>Please select the file containing the mapping of City to coordinates information</p>" +
                "<p>The format of the file must match these parameters: <code>lid|continent|country|region|city|lat|lon|pop</code></p>" +
                "<p>An example of valid data would be <code>Altamonte Springs-FL-US|NA|US|FL|Altamonte Springs|28.66111|-81.36562|0</code> <strong>NOTE:</strong> Only the following parameters are used for the dataset: <code>continent, country, city, lat, lon</code></p>" +
                "<p>All other lines (not matching this format) will be ignored, <u>make sure to remove unneeded lines by yourself to help speedup the processing of the file</u></p>" +
                "<p>You can obtain these files from <a href=\"http://data.caida.org/datasets/as-relationships-geo/\" target=\"_blank\">caida.org</a></p>" +
                "<p>The expected file format is <code>.txt</code> - so please extract the <code>.gz</code> file from caida.org before uploading</p><hr/>" +
                "<p><strong>AS-Rel-Geo Information</strong></p>" +
                "<p>Please select the file containing the mapping of AS-Number to location information</p>" +
                "<p>The format of the file must match these parameters: <code>AS0|AS1|loc0,source0.0,source0.1,loc1|loc1,source1.0...</code></p>" +
                "<p>An example of valid data would be <code>3|3356|Boston-MA-US,bc|San Jose-CA-US,bc</code></p>" +
                "<p>All other lines (not matching this format) will be ignored, <u>make sure to remove unneeded lines by yourself to help speedup the processing of the file</u></p>" +
                "<p>You can obtain these files from <a href=\"http://data.caida.org/datasets/as-relationships-geo/\" target=\"_blank\">caida.org</a></p>" +
                "<p>The expected file format is <code>.txt</code> - so please extract the <code>.gz</code> file from caida.org before uploading</p><hr/>";
    }

    @Override
    public String getExpectedFormat() {
        return "Uncompressed Tab-Separated textfile";
    }

    @Override
    public Map<String, String> getAdditionalFields() {
        HashMap<String, String> fields = new HashMap<>();
        fields.put("organizations", "file");
        fields.put("locations", "file");
        fields.put("as-rel-geo", "file");
        return fields;
    }

    public void parseInformation(Object data, Map<String, Object> params) throws FormatNotSupportedException {
        logger.debug("starting import of data");

        checkIfAdditionalParamsSadisfied(params);

        String strData = "";
        strData = getStringFromData(data);

        logger.debug("data format accepted - begin parsing");

        parseASLinks(strData);

        logger.debug("parsing finished, parsed {} edges and {} nodes",importedEdges.size(), importedNodes.size() );
        logger.debug("starting parsing of organization data");

        Object orgData = params.get("organizations");
        strData = getStringFromData(orgData);

        Map<String, String> orgs = new HashMap<>();

        parseOrganizations(strData, orgs);

        logger.debug("finished parsing of organization data, parsed {} ids", orgs.size());
        logger.debug("starting matching of as names to org ids");

        matchAsNamesToOrganizations(orgs);

        logger.debug("starting import of as-rel-geo");
        Object relData = params.get("as-rel-geo");
        strData = getStringFromData(relData);

        Map<String, String> relAsCity = new HashMap<>();

        parseAsRelGeoInformation(strData, relAsCity);

        logger.debug("starting import of location data");
        Object locData = params.get("locations");
        strData = getStringFromData(locData);

        Map<String, GeoData> relCityGeo = new HashMap<>();

        parseLocationsInformation(strData, relCityGeo);
        logger.debug("finished reading additional data, found {} AS relations and {} geo infos", relAsCity.size(), relCityGeo.size());
        logger.debug("starting mapping of geo information");

        mapLocationToGeo(relAsCity, relCityGeo);

        logger.debug("finished matching org ids");
    }

    private void checkIfAdditionalParamsSadisfied(Map<String, Object> params) throws FormatNotSupportedException {
        //check if mandatory parameters are present
        for(String param : getAdditionalFields().keySet()){
            if(params.get(param) == null){
                throw new FormatNotSupportedException("The mandatory field " + param + " ist not filled");
            }
        }
    }

    private void mapLocationToGeo(Map<String, String> relAsCity, Map<String, GeoData> relCityGeo) {
        for(CoriaNode node : getParsedNodes()){
            String cityInfo = relAsCity.get(node.getAsid());
            if(cityInfo != null){
                GeoData geoData = relCityGeo.get(cityInfo);
                if(geoData != null){
                    node.setAttribute("geo", "true");
                    node.setAttribute("geo_city", geoData.city);
                    node.setAttribute("geo_country", geoData.country.toLowerCase());
                    node.setAttribute("geo_continent", geoData.continent);
                    node.setAttribute("geo_latitude", geoData.lat);
                    node.setAttribute("geo_longitude", geoData.lon);
                }
            }
        }
    }

    private void parseLocationsInformation(String strData, Map<String, GeoData> relCityGeo) {
        for(String line : strData.split("\n")){
            if(line.contains("\r")){line = line.replaceAll("\r","");}
            if(!line.startsWith("#")){                                  //ignore lines which comments
                String [] parts = line.split("\\|");
                if(parts.length >= 8){                              //ignore lines with not enough data
                    if(!parts[0].equals("")){
                        //no whitespaces allowed in city
                        if(relCityGeo.get(parts[0]) == null) {
                            relCityGeo.put(parts[0], new GeoData(parts[4], parts[1], parts[2], parts[5], parts[6]));
                        }
                    }else{
                        logger.trace("ignoring line because of invalid whitespace [in name|prefix]: " + line);
                    }
                }else{
                    logger.trace("ignoring line because it contains not enough values: " + line);
                    }
            }else{
                logger.trace("ignoring line because it is a comment: " + line);
            }
        }
    }

    private void parseAsRelGeoInformation(String strData, Map<String, String> relAsCity) {
        Object relData;
        for(String line : strData.split("\n")){
            if(line.contains("\r")){line = line.replaceAll("\r","");}
            if(!line.startsWith("#")){                                  //ignore lines which comments
                if(line.substring(0,1).matches("[0-9]")){       //only use lines starting with a number
                    String [] parts = line.split("\\|");
                    if(parts.length >= 3){                              //ignore lines with not enough data
                        if(!parts[1].contains(" ") && !parts[2].equals("")){
                            //no whitespaces allowed in as
                            if(relAsCity.get(parts[1]) == null) {
                                relAsCity.put(parts[1], parts[2].split(",")[0]);
                            }
                        }else{
                            logger.trace("ignoring line because of invalid whitespace [in name|prefix]: " + line);
                        }
                    }else{
                        logger.trace("ignoring line because it contains not enough values: " + line);
                    }
                }else{
                    logger.trace("ignoring line because of invalid prefix: " + line);
                }
            }else{
                logger.trace("ignoring line because it is a comment: " + line);
            }
        }
        relData = null;
    }

    private void matchAsNamesToOrganizations(Map<String, String> orgs) {
        for(CoriaEdge edge : importedEdges){
//            String sourceOrg = orgs.get(edge.getSourceNode());
//            String destOrg = orgs.get(edge.getDestinationNode());
//
//            if(sourceOrg != null){
//                edge.setName(edge.getName().replace(edge.getSourceNode(), sourceOrg));
//                edge.setSourceNode(sourceOrg);
//            }
//            if(destOrg != null){
//                edge.setName(edge.getName().replace(edge.getDestinationNode(), destOrg));
//                edge.setDestinationNode(destOrg);
//            }
        }
        for(CoriaNode node : importedNodes){
            String org = orgs.get(node.getName());
            if(org != null){
                node.setAsid(node.getName());
                node.setName(org);
            }
        }
    }

    private void parseOrganizations(String strData, Map<String, String> orgs) {
        for(String line : strData.split("\n")){
            if(line.contains("\r")){line = line.replaceAll("\r","");}
            if(!line.startsWith("#")){                                  //ignore lines which comments
                if(line.substring(0,1).matches("[0-9]")){       //only use lines starting with a number
                    String [] parts = line.split("\\|");
                    if(parts.length >= 5){                              //ignore lines with not enough data
                        if(!parts[0].contains(" ") && !parts[2].equals("")){
                            //no whitespaces allowed in as
                            orgs.put(parts[0], parts[2]);
                        }else{
                            logger.trace("ignoring line because of invalid whitespace [in name|prefix]: " + line);
                        }
                    }else{
                        logger.trace("ignoring line because it contains not enough values: " + line);
                    }
                }else{
                    logger.trace("ignoring line because of invalid prefix: " + line);
                }
            }else{
                logger.trace("ignoring line because it is a comment: " + line);
            }
        }
    }

    private void parseASLinks(String strData) {
        List<String> nodeDict = new ArrayList<>();
        for(String line : strData.split("\n")){
            if(line.contains("\r")){line = line.replaceAll("\r","");}
            if(!line.startsWith("#")){                                  //ignore lines which comments
                if(line.startsWith("I") || line.startsWith("D")){       //only use direct and indirect links
                    String [] parts = line.split("\t");
                    if(parts.length >= 3){                              //ignore lines with not enough data
                        if(parts[0].length() == 1 && !parts[1].contains(" ") && !parts[2].contains(" ")){
                            //no whitespaces allowed in names
                            String [] fromParts = parts[1].split(",");
                            String [] toParts = parts[2].split(",");

                            for(String fPart : fromParts){
                                for(String tPart : toParts){
                                    importedEdges.add(new CoriaEdge("", fPart, tPart));
                                    if(!nodeDict.contains(fPart)) {
                                        importedNodes.add(new CoriaNode(fPart));
                                        nodeDict.add(fPart);
                                    }
                                    if(!nodeDict.contains(tPart)) {
                                        importedNodes.add(new CoriaNode(tPart));
                                        nodeDict.add(tPart);
                                    }
                                }
                            }
                        }else{
                            logger.trace("ignoring line because of invalid whitespace [in name|prefix]: " + line);
                        }
                    }else{
                        logger.trace("ignoring line because it contains not enough values: " + line);
                    }
                }else{
                    logger.trace("ignoring line because of invalid prefix: " + line);
                }
            }else{
                logger.trace("ignoring line because it is a comment: " + line);
            }
        }
    }

    private class GeoData {
        String city;
        String continent;
        String country;
        String lat;
        String lon;

        public GeoData(String city, String continent, String country, String lat, String lon) {
            this.city = city;
            this.continent = continent;
            this.country = country;
            this.lat = lat;
            this.lon = lon;
        }
    }

    @Override
    public List<CoriaEdge> getParsedEdges() {
        return importedEdges;
    }

    @Override
    public List<CoriaNode> getParsedNodes() {
        return importedNodes;
    }

    private String getStringFromData(Object data) throws FormatNotSupportedException {
        String strData;
        if(data instanceof byte[]){
            //we got an byte array -> convert to string before parsing
            strData = new String(((byte[]) data), Charset.defaultCharset());
        }else if(data instanceof String){
            //data is in string format -> no need to convert
            strData = (String) data;
        }else{
            //the given format is not supported -> exception
            logger.error("The provided format is not supported, please provide data in String od byte[] format!");
            throw new FormatNotSupportedException("The provided format is not supported, please provide data in String or byte[] format!");
        }
        return strData;
    }

    @Override
    public String toString() {
        return "ASLinksEdgeResolveGeoImporter{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
