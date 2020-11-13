package com.coria.v3.parser;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.UploadedFile;

import java.util.List;

/**
 * These parsers are used by CORIA to parse a set of uploaded files and process them into a single, database-persistable DatasetEntity.<br/>
 * They separate the content and create an internal graph representation before saving its result to the database.
 * Created by Sebastian Gross, 2017 (coria-core/src/main/java/com/bigbasti/coria/parser/ImportModule.java)
 * Modified by David Fradin, 2020: Simplified and refactored into other classes to reduce code redundancy.
 */
public abstract class DatasetEntityImportModule extends ImportModuleBase {

    public DatasetEntityImportModule(String id, String name, String description) {
        super(id, name, description);
    }

    public abstract DatasetEntity parseInformation(String datasetName, List<UploadedFile> files, RepositoryManager repositoryManager) throws Exception;
}
