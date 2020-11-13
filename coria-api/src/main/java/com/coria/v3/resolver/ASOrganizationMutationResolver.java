package com.coria.v3.resolver;

import com.coria.v3.dbmodel.ASOrganizationEntity;
import com.coria.v3.parser.ASOrganizationEntityImportModule;
import com.coria.v3.utility.UploadedFile;
import graphql.ErrorType;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.Part;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL mutation requests for ASOrganizationEntity.
 */
@Component
public class ASOrganizationMutationResolver extends BaseResolver implements GraphQLMutationResolver {
    private static final Logger logger = LoggerFactory.getLogger(ASOrganizationMutationResolver.class);

    /**
     * Imports an uploaded file with ASOrganization entries into the DB.
     *
     * @param importModule ID of the ASOrganizationEntityImportModule
     * @param files        The uploaded file(s)
     * @param environment  Request object
     * @return The first ASOrganizationEntity of the imported list.
     */
    @SuppressWarnings("unused")
    public ASOrganizationEntity createASOrganization(String importModule, List<Part> files, DataFetchingEnvironment environment) {
        try {
            Instant starts = Instant.now();
            logger.debug(environment.getArguments().toString());
            if (files.size() == 0)
                throw buildException("Please provide a file to import.", ErrorType.ValidationError);
            ASOrganizationEntityImportModule asOrganizationEntityImportModule = appContext.getASOrganizationEntityImportModule(importModule);
            if (asOrganizationEntityImportModule == null)
                throw buildException(String.format("No import module found with id %s", importModule), ErrorType.ValidationError);
            List<ASOrganizationEntity> asOrganizationEntities = asOrganizationEntityImportModule.parseInformation(files.stream().map(UploadedFile::new).collect(Collectors.toList()), repositoryManager);
            logger.debug("createDataset finished: {}", Duration.between(starts, Instant.now()).toString());
            if (asOrganizationEntities.size() == 0) {
                throw buildException("No valid AS organizations found in the uploaded files.", ErrorType.ValidationError);
            }
            return asOrganizationEntities.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw buildException("Error while importing AS organizations: " + e.getMessage(), ErrorType.ValidationError);
        }
    }
}
