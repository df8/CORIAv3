package com.coria.v3.caida.location;

import com.coria.v3.caida.CaidaFileType;
import com.coria.v3.dbmodel.ASLocationEntity;
import com.coria.v3.parser.ASLocationEntityImportModule;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.coria.v3.caida.CaidaFileType.FileType.ASLocationsList;
import static com.coria.v3.caida.CaidaFileType.REGEX_ASLocationsList_LINE;
import static com.coria.v3.caida.CaidaFileType.processFileType;

/**
 * Created by David Fradin, 2020
 */
public class CaidaASLocationEntityImportModule extends ASLocationEntityImportModule {
    private final Logger logger = LoggerFactory.getLogger(CaidaASLocationEntityImportModule.class);

    public CaidaASLocationEntityImportModule(String id, String name, String description) {
        super(id, name, description);
    }

    @Override
    public List<ASLocationEntity> parseInformation(List<UploadedFile> files, RepositoryManager repositoryManager) throws Exception {
        Instant start = Instant.now();
        HashMap<String, ASLocationEntity> locations = new HashMap<>();
        // Step 1: Since the user is allowed to upload multiple files, we first check that all files are compatible.
        CaidaFileType.FileType[] fileTypes = CaidaFileType.classifyFiles(files);
        for (int i = 0; i < fileTypes.length; i++) {
            if (fileTypes[i] != CaidaFileType.FileType.ASLocationsList) {
                throw new Exception("Unsupported file format: " + files.get(i).getSubmittedFileName());
            }
        }

        //Step 2: Process all AS Location files
        processFileType(files, fileTypes, ASLocationsList, null, true, REGEX_ASLocationsList_LINE, (line) -> {
            String[] parts = line.split("\\|");
            /*
            parts[0] = location ID
            parts[1] = continent ID
            parts[2] = country ID
            parts[3] = region ID
            parts[4] = city name
            parts[5] = latitude coordinate
            parts[6] = longitude coordinate
             */
            ASLocationEntity prevAsLocationEntity = locations.get(parts[0]);
            ASLocationEntity newAsLocationEntity = new ASLocationEntity(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6]);
            if (prevAsLocationEntity == null) {
                locations.put(parts[0], newAsLocationEntity);
            } else if (!prevAsLocationEntity.equals(newAsLocationEntity)) {
                throw new Exception(String.format("Found two different AS location entries with the same key: %s vs. %s", prevAsLocationEntity.toString(), newAsLocationEntity.toString()));
            }
        });

        //Step 3: Persist
        logger.debug("Started persisting...");
        repositoryManager.getAsLocationRepository().saveAll(locations.values());
        logger.debug("Finished persisting. {}", Duration.between(start, Instant.now()));
        return new ArrayList<>(locations.values());
    }
}