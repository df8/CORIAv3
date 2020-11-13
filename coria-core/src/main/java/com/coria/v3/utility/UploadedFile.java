package com.coria.v3.utility;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by David Fradin, 2020
 * Tomcat Server passes on uploaded files as Part objects.
 * This is a helper implementation for all CORIA code handling uploaded files (mainly Resolvers and Import modules).
 *
 * @see javax.servlet.http.Part
 */
public class UploadedFile implements Part {
    private final Part part;

    public UploadedFile(Part part) {
        this.part = part;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    @Override
    public String getContentType() {
        return part.getContentType();
    }

    @Override
    public String getName() {
        return part.getName();
    }

    @Override
    public String getSubmittedFileName() {
        return part.getSubmittedFileName();
    }

    @Override
    public long getSize() {
        return part.getSize();
    }

    @Override
    public void write(String s) throws IOException {
        part.write(s);
    }

    @Override
    public void delete() throws IOException {
        part.delete();
    }

    @Override
    public String getHeader(String s) {
        return part.getHeader(s);
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return part.getHeaders(s);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return part.getHeaderNames();
    }
}
