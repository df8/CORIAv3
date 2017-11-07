package com.bigbasti.coria.model;

import org.hibernate.validator.internal.util.StringHelper;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Sebastian Gross
 */
public class DataSetUpload {
    private String name;
    private String parser;
    private MultipartFile file;

    public DataSetUpload() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParser() {
        return parser;
    }

    public void setParser(String parser) {
        this.parser = parser;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    /**
     * performs a self check by checking if all mandatory fields are set
     * @return
     */
    public boolean isValid(){
        return !StringHelper.isNullOrEmptyString(name)
                && !StringHelper.isNullOrEmptyString(parser)
                && file != null;
    }

    @Override
    public String toString() {
        return "DataSetUpload{" +
                "name='" + name + '\'' +
                ", parser='" + parser + '\'' +
                '}';
    }
}
