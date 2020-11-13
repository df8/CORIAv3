package com.coria.v3.parser;

import com.coria.v3.dbmodel.ASLocationEntity;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.UploadedFile;

import java.util.List;

/**
 * Created by David Fradin, 2020
 * Classes extending ASLocationEntityImportModule are built to parse a set of uploaded files and process them into list of database-persistable ASLocationEntity objects.
 */
public abstract class ASLocationEntityImportModule extends ImportModuleBase {
    public ASLocationEntityImportModule(String id, String name, String description) {
        super(id, name, description);
    }

    public abstract List<ASLocationEntity> parseInformation(List<UploadedFile> files, RepositoryManager repositoryManager) throws Exception;
}
