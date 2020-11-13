package com.coria.v3.parser;

import com.coria.v3.dbmodel.ASOrganizationEntity;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.UploadedFile;

import java.util.List;

/**
 * Created by David Fradin, 2020
 * Classes extending ASOrganizationEntityImportModule are built to parse a set of uploaded files and process them into list of database-persistable ASOrganizationEntity objects.
 */
public abstract class ASOrganizationEntityImportModule extends ImportModuleBase {
    public ASOrganizationEntityImportModule(String id, String name, String description) {
        super(id, name, description);
    }

    public abstract List<ASOrganizationEntity> parseInformation(List<UploadedFile> files, RepositoryManager repositoryManager) throws Exception;
}
