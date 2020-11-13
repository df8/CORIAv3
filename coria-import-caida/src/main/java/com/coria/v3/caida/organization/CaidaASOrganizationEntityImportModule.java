package com.coria.v3.caida.organization;

import com.coria.v3.caida.CaidaFileType;
import com.coria.v3.dbmodel.ASOrganizationEntity;
import com.coria.v3.parser.ASOrganizationEntityImportModule;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.coria.v3.caida.CaidaFileType.FileType.ASOrganizationsList;
import static com.coria.v3.caida.CaidaFileType.REGEX_ASOrganizationsList_LINE;
import static com.coria.v3.caida.CaidaFileType.processFileType;

/**
 * Created by David Fradin, 2020
 */
public class CaidaASOrganizationEntityImportModule extends ASOrganizationEntityImportModule {
    private final Logger logger = LoggerFactory.getLogger(CaidaASOrganizationEntityImportModule.class);

    public CaidaASOrganizationEntityImportModule(String id, String name, String description) {
        super(id, name, description);
    }

    @Override
    public List<ASOrganizationEntity> parseInformation(List<UploadedFile> files, RepositoryManager repositoryManager) throws Exception {
        Instant start = Instant.now();
        HashMap<String, ASOrganizationEntity> organizations = new HashMap<>();
        // Step 1: Since the user is allowed to upload multiple files, we first check that all files are compatible.
        CaidaFileType.FileType[] fileTypes = CaidaFileType.classifyFiles(files);
        for (int i = 0; i < fileTypes.length; i++) {
            if (fileTypes[i] != CaidaFileType.FileType.ASOrganizationsList) {
                throw new Exception("Unsupported file format: " + files.get(i).getSubmittedFileName());
            }
        }

        //Step 2: Process all AS Organization files
        processFileType(files, fileTypes, ASOrganizationsList, null, false, REGEX_ASOrganizationsList_LINE, (line) -> {
            String[] parts = line.split("\\|");
            /*
            parts[0] = organization ID
            parts[1] = date of last entry change
            parts[2] = organization name
            parts[3] = country ID
            parts[4] = WHOIS database source name
             */

            ASOrganizationEntity prevAsOrganizationEntity = organizations.get(parts[0]);
            ASOrganizationEntity newAsOrganizationEntity = new ASOrganizationEntity(parts[0], parts[2], parts[3], parts[4]);
            if (prevAsOrganizationEntity == null) {
                organizations.put(parts[0], newAsOrganizationEntity);
            } else if (!prevAsOrganizationEntity.equals(newAsOrganizationEntity)) {
                throw new Exception(String.format("Found two different AS organization entries with the same key: %s vs. %s", prevAsOrganizationEntity.toString(), newAsOrganizationEntity.toString()));
            }
        });

        //Step 3: Persist
        logger.debug("Started persisting...");
        repositoryManager.getAsOrganizationRepository().saveAll(organizations.values());
        logger.debug("Finished persisting. {}", Duration.between(start, Instant.now()));
        return new ArrayList<>(organizations.values());
    }
}