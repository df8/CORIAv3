package com.coria.v3.resolver;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.parser.DatasetEntityImportModule;
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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL mutation requests for DatasetEntity.
 * Source: https://www.pluralsight.com/guides/building-a-graphql-server-with-spring-boot
 */
@Component
public class DatasetMutationResolver extends BaseResolver implements GraphQLMutationResolver {
    private static final Logger logger = LoggerFactory.getLogger(DatasetMutationResolver.class);

    /**
     * Imports an uploaded Dataset with ASOrganization entries into the DB. Multiple files are supported.
     *
     * @param importModule ID of the DatasetEntityImportModule
     * @param files        The uploaded file(s)
     * @param environment  Request object
     * @return the created DatasetEntity
     */
    @SuppressWarnings("unused")
    public DatasetEntity createDataset(String importModule, String name, List<Part> files, DataFetchingEnvironment environment) {
        try {
            Instant starts = Instant.now();
            logger.debug(environment.getArguments().toString());
            if (files.size() == 0)
                throw buildException("Please provide a file to import.", ErrorType.ValidationError);
            DatasetEntityImportModule datasetEntityImportModuleObj = appContext.getDatasetEntityImportModule(importModule);
            if (datasetEntityImportModuleObj == null)
                throw buildException(String.format("No import module found with id %s", importModule), ErrorType.ValidationError);
            DatasetEntity datasetEntity = datasetEntityImportModuleObj.parseInformation(name, files.stream().map(UploadedFile::new).collect(Collectors.toList()), repositoryManager);
            logger.debug("createDataset finished: {}", Duration.between(starts, Instant.now()).toString());
            return datasetEntity;
        } catch (Exception e) {
            e.printStackTrace();
            throw buildException("Error while importing dataset: " + e.getMessage(), ErrorType.ValidationError);
        }

    }

    /**
     * Handles the GraphQL request to rename a dataset.
     *
     * @param datasetId Dataset UUID
     * @param name      new dataset name
     * @return the modified DatasetEntity
     */
    @SuppressWarnings("unused")
    public DatasetEntity updateDataset(UUID datasetId, String name) {
        DatasetEntity datasetEntity = repositoryManager.getDatasetRepository().findById(datasetId).orElse(null);
        if (datasetEntity == null)
            throw buildException("Dataset not found.", ErrorType.ValidationError);
        datasetEntity.setName(name);
        repositoryManager.getDatasetRepository().save(datasetEntity);
        return datasetEntity;
    }

    /**
     * Handles the GraphQL request to delete a dataset including all metrics, nodes, edges and all metric results.
     *
     * @param datasetId Dataset UUID
     * @return the removed DatasetEntity
     */
    @SuppressWarnings("unused")
    public DatasetEntity deleteDataset(UUID datasetId) {
        DatasetEntity datasetEntity = repositoryManager.getDatasetRepository().findById(datasetId).orElseThrow();
        try {
            repositoryManager.getDatasetRepository().delete(datasetEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datasetEntity;
    }
}
