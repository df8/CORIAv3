package com.bigbasti.coria.export;

/**
 * contains additional meta inforamtion about a successful export
 */
public class ExportResult {
    private String contentType;
    private String fileName;
    private Object exportResult;

    public ExportResult() {
    }

    public ExportResult(String contentType, String fileName, Object exportResult) {
        this.contentType = contentType;
        this.fileName = fileName;
        this.exportResult = exportResult;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Object getExportResult() {
        return exportResult;
    }

    public void setExportResult(Object exportResult) {
        this.exportResult = exportResult;
    }

    @Override
    public String toString() {
        return "ExportResult{" +
                "contentType='" + contentType + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
