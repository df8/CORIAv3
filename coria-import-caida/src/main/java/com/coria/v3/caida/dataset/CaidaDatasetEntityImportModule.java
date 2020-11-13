package com.coria.v3.caida.dataset;

import com.coria.v3.caida.CaidaFileType;
import com.coria.v3.dbmodel.*;
import com.coria.v3.parser.DatasetEntityImportModule;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.coria.v3.caida.CaidaFileType.*;
import static com.coria.v3.caida.CaidaFileType.FileType.*;

/**
 * The user may provide one or multiple files. This class will automatically classify the type of each file and parse them all into one DatasetEntity object.
 * TODO /2: Inserting a large number of rows into a DB is still relatively slow with JPA/Hibernate due to lots and lots of "field by field dirty checking".
 * Switching to JDBC could improve the import processing time. More information on Hibernate dirty checking: https://stackoverflow.com/a/42212442/4520273
 * <p>
 * TODO /3 CAIDA allows an automatic download of all necessary files - maybe an option for later?
 * <p>
 * Created by David Fradin, 2020
 * Adopted from parsers by Sebastian Gross, 2017 (coria-import-caida/src/main/java/com/bigbasti/coria/aslinks/ASLinksEdgeResolveImporter.java)
 */
public class CaidaDatasetEntityImportModule extends DatasetEntityImportModule {
    private final Logger logger = LoggerFactory.getLogger(CaidaDatasetEntityImportModule.class);

    public CaidaDatasetEntityImportModule(String id, String name, String description) {
        super(id, name, description);
    }

    @Override
    public DatasetEntity parseInformation(String datasetName, List<UploadedFile> files, RepositoryManager repositoryManager) throws Exception {
        Instant start = Instant.now();
        this.nodeDict = new HashMap<>();
        this.edgeDictByKey = new HashMap<>();
        this.edgeDictByName = new HashMap<>();

        // Step 1: Since the user is allowed to upload multiple files of different CAIDA types, we need to classify all files.
        CaidaFileType.FileType[] fileTypes = CaidaFileType.classifyFiles(files);
        boolean asOrgCountChecked = false, asLocationCountChecked = false;
        for (FileType fileType : fileTypes) {

            // We must make sure that the AS Organization table has been populated with values first.
            if (fileType == AStoOrganizationMapping && !asOrgCountChecked) {
                asOrgCountChecked = true;
                if (repositoryManager.getAsOrganizationRepository().count() <= 0)
                    throw new Exception("Table as_organization is empty. Please import the list of AS organizations first.");
            }
            // We must make sure that the AS Location table has been populated with values first.
            if (fileType == ASLinkToGeoLocationMapping && !asLocationCountChecked) {
                asLocationCountChecked = true;
                if (repositoryManager.getAsLocationRepository().count() <= 0)
                    throw new Exception("Table as_location is empty. Please import the list of AS organizations first.");
            }
        }

        logger.debug("Finished classifying each uploaded file. {}", Duration.between(start, Instant.now()));
        start = Instant.now();
        DatasetEntity datasetEntity = new DatasetEntity();
        datasetEntity.setId(UUID.randomUUID());
        datasetEntity.setCreated(Timestamp.from(Instant.now()));
        datasetEntity.setName(datasetName);
        //Store the names of all uploaded files
        datasetEntity.addAttribute("uploaded_filenames", files.stream().map(UploadedFile::getSubmittedFileName).collect(Collectors.joining("\t")));

        // Step 2: Process all CaidaFileType.AStoAS_Links files into edges and nodes
        processFileType(files, fileTypes, AStoAS_Links, new String[]{"#", "M", "T"}, true, REGEX_AStoAS_Links_LINE, (line) -> {
            String[] parts = line.split("\t");
            /*
            parts[0] = type of scanned link, direct (letter D) or indirect (letter I).
            parts[1] = from_AS
            parts[2] = to_AS
            parts[3] = for indirect links, this is the gap_length. For directed links this is the ID of the first monitor to detect
            parts[4+] = IDs of monitors that reported this link
            Monitors are end clients running network tracing. For measurement infrastructure details see https://www.caida.org/projects/ark/
             */
            Integer gap_length = null;
            int count_monitors = -1;
            String from_AS = formatAsId(parts[1]);
            String to_AS = formatAsId(parts[2]);
            if (parts[0].equals("D")) {
                count_monitors = parts.length - 3;
            } else if (parts[0].equals("I")) {
                gap_length = Integer.valueOf(parts[3]);
                count_monitors = parts.length - 4;
            }
            if (count_monitors > -1) {
                NodeEntity fNode = createNode(from_AS);
                NodeEntity tNode = createNode(to_AS);
                createEdge(datasetEntity, fNode, tNode, parts[0], count_monitors, gap_length);
                datasetEntity.addNode(fNode);
                datasetEntity.addNode(tNode);
            }
        });
        repositoryManager.getDatasetRepository().save(datasetEntity); //Persist all nodes

        datasetEntity.setEdges(edgeDictByName.values());
        repositoryManager.getDatasetRepository().save(datasetEntity);//Persist all edges
        logger.debug("Finished parsing AS links. {}", Duration.between(start, Instant.now()));
        start = Instant.now();

        datasetEntity.addAttribute("count_nodes", String.valueOf(nodeDict.size()));
        datasetEntity.addAttribute("count_edges", String.valueOf(datasetEntity.getEdges().size()));

        // Step 3: Process all AStoOrganizationMapping files into node references
        Map<String, ASOrganizationEntity> asOrganizationMap = repositoryManager.getAsOrganizationRepository().findAll().stream().collect(Collectors.toMap(ASOrganizationEntity::getId, asOrganizationEntity -> asOrganizationEntity));
        AtomicLong nodesNotFound = new AtomicLong(0);
        AtomicLong matchesFound = new AtomicLong(0);
        processFileType(files, fileTypes, AStoOrganizationMapping, null, true, REGEX_AStoOrganizationMapping_LINE, (line) -> {
            String[] parts = line.split("\\|");
            /*
            parts[0] = autonomous system ID
            parts[1] = entry change date
            parts[2] = autonomous system name
            parts[3] = organization ID (reference)
            parts[4] = opaque_id
            parts[5] = source e.g. one of ARIN|RIPE|APNIC|LACNIC|AFRINIC|JPNIC
             */
            ASOrganizationEntity org = asOrganizationMap.get(parts[3]);
            if (org != null) {
                NodeEntity node = nodeDict.get(formatAsId(parts[0]));
                if (node != null) {
                    matchesFound.getAndIncrement();
                    node.addOrganization(org, parts[1]);
                } else {
                    nodesNotFound.getAndIncrement();
                    //logger.debug(String.format("Node %s not found. Referenced in line \"%s\"", formatAsId(parts[0]), line));
                    //throw new Exception(String.format("Node %s not found. Referenced in line \"%s\"", formatAsId(parts[0]), line));
                }
            } else {
                throw new Exception(String.format("AS organization %s not found", parts[3]));
            }
        });
        repositoryManager.getDatasetRepository().save(datasetEntity);//Persist all organizations
        logger.debug("Finished parsing AS-to-Organization mappings. Matches found: {}. Nodes not found: {}. {}", matchesFound.get(), nodesNotFound.get(), Duration.between(start, Instant.now()));
        start = Instant.now();
        matchesFound.set(0);
        nodesNotFound.set(0);
        // Step 4: Process all CaidaFileType.AStoTypeMapping files into node attributes
        processFileType(files, fileTypes, AStoTypeMapping, null, true, REGEX_AStoTypeMapping_LINE, (line) -> {
            String[] parts = line.split("\\|");
            /*
            parts[0] = autonomous system ID
            parts[1] = source for classification
            parts[2] = class assigned e.g. one of "Content", "Enterprise", "Transit/Access"
             */
            NodeEntity node = nodeDict.get(formatAsId(parts[0]));
            if (node != null) {
                matchesFound.getAndIncrement();
                //Merge conflict handling:
                String prevClass = node.getAttributes().getOrDefault("as_class", null);
                if (prevClass == null) {
                    node.addAttribute("as_class", parts[2]);
                } else if (!prevClass.equals(parts[2])) {
                    throw new Exception(String.format("Merge conflict in AStoTypeMapping: Node %s has two AS class assignments: %s and %s", node.getName(), prevClass, parts[2]));
                }
            } else {
                nodesNotFound.getAndIncrement();
            }
        });
        logger.debug("Finished parsing AS-to-Type mappings. Matches found: {}. Nodes not found: {}. {}", matchesFound.get(), nodesNotFound.get(), Duration.between(start, Instant.now()));

        // Step 5: Process all CaidaFileType.AStoLocationMapping files into node references
        Map<String, ASLocationEntity> asLocationMap = new HashMap<>();
        repositoryManager.getAsLocationRepository().findAll().forEach(asLocationEntity -> asLocationMap.put(asLocationEntity.getId(), asLocationEntity));
        AtomicLong edgesNotFound = new AtomicLong(0);
        matchesFound.set(0);
        processFileType(files, fileTypes, ASLinkToGeoLocationMapping, null, true, REGEX_ASLinkToGeoLocationMapping_LINE, (line) -> {
            String[] parts = line.split("\\|");
            /*
            # format: AS0|AS1|loc0,source0.0,source0.1,loc1|loc1,source1.0...
            parts[0] = autonomous system ID 0
            parts[1] = autonomous system ID 1
            parts[2+] = location IDs and sources such as bc,mlp,edge,lg
             */
            parts[0] = formatAsId(parts[0]);
            parts[1] = formatAsId(parts[1]);
            EdgeEntity edge = edgeDictByName.get(parts[0] + " <-> " + parts[1]);//first try
            if (edge == null) {//second try
                edge = edgeDictByName.get(parts[1] + " <-> " + parts[0]);
                if (edge == null) {
                    edgesNotFound.incrementAndGet();
                }
            }
            if (edge != null) {
                for (int i = 2; i < parts.length; i++) {
                    String[] locationParts = parts[i].split(",", 2);//split at the first comma
                        /*
                        locationParts[0] = AS Location ID e.g. Los Angeles-CA-US,bc
                        locationParts[1] = Comma-separated list of sources. CAIDA used four different methods to find AS link locations:
                         bc,mlp,edge,lg
                         */
                    ASLocationEntity loc = asLocationMap.get(locationParts[0]);
                    if (loc != null) {
                        matchesFound.getAndIncrement();
                        edge.addLocation(loc, locationParts[1]);
                    } else {
                        throw new Exception(String.format("AS location %s not found", parts[3]));
                    }
                }
            }
        });
        repositoryManager.getDatasetRepository().save(datasetEntity);//Persist all locations
        logger.debug("Finished parsing AS Link-to-Location mappings. Matches found: {}. Nodes not found: {}. {}", matchesFound.get(), nodesNotFound.get(), Duration.between(start, Instant.now()));

        logger.debug("Started persisting...");
        start = Instant.now();
        repositoryManager.getDatasetRepository().save(datasetEntity);
        logger.debug("Finished persisting. {}", Duration.between(start, Instant.now()));
        return datasetEntity;
    }

    HashMap<String, NodeEntity> nodeDict;
    HashMap<String, EdgeEntity> edgeDictByKey;
    HashMap<String, EdgeEntity> edgeDictByName;

    //private NodeEntity createNode(String nodeName, ImportedStreamElement importedStreamElement) {
    private NodeEntity createNode(String nodeName) {
        NodeEntity fNode = nodeDict.get(nodeName);
        if (fNode == null) {
            //logger.debug("Imported Node={}", fPart);
            fNode = new NodeEntity();
            fNode.setId(UUID.randomUUID());
            fNode.setName(nodeName);
            nodeDict.put(nodeName, fNode);
            //importedNodes.add(fNode);
            //importedStreamElement.nodes.add(fNode);
        }
        return fNode;
    }

    private void createEdge(DatasetEntity datasetEntity, NodeEntity fNode, NodeEntity tNode, String asLinkType, long count_monitors, Integer gap_length) {
        String name = String.format("%s <-> %s", fNode.getName(), tNode.getName());
        // Two lines are considered duplicates if: it is a direct link with the same values for from_AS and to_AS or
        // if is an indirect link with the same values for from_AS, to_AS and gap_length.
        String key;
        if (asLinkType.equals("I")) {
            key = String.format("%s$%s$%s$%d", asLinkType, fNode.getName(), tNode.getName(), gap_length);
        } else {
            key = String.format("%s$%s$%s", asLinkType, fNode.getName(), tNode.getName());
        }
        EdgeEntity edge = edgeDictByKey.get(key);
        if (edge == null) {
            edge = new EdgeEntity();
            edge.setName(name);
            edge.setDataset(datasetEntity);
            edge.setNodeSource(fNode);
            edge.setNodeTarget(tNode);
            edge.addAttribute("as_link_type", asLinkType);
            edge.addAttribute("count_monitors", String.valueOf(count_monitors));
            if (gap_length != null)
                edge.addAttribute("gap_length", String.valueOf(gap_length));
            //dual lookup:
            edgeDictByName.put(name, edge);
            edgeDictByKey.put(key, edge);
        }
    }

    protected static String formatAsId(String id) {
        return "AS" + "0".repeat(Math.max(0, 5 - id.length())) + id;//builds AS00002 instead of "2"
    }

}
