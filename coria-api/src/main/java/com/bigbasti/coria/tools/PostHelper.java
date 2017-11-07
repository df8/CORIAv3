package com.bigbasti.coria.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PostHelper {
    private static Logger logger = LoggerFactory.getLogger(PostHelper.class);

    /**
     * Reads all provided additional parameters from a post request.<br/>
     * Additional parameters always start with additional_<br/>
     * All read values are converted to strings and returned
     * @param request The HTTP Request to read the parameters from
     * @return Map<String, Object> containing the read parameters, using everything that comes after the _ as key
     */
    public static Map<String, Object> getAdditionalParameters(HttpServletRequest request){
        Map<String, Object> params = new HashMap<>();

        try {
            //load all additional text fields
            List<Part> additionalParts = request.getParts()
                    .stream()
                    .filter(part -> part.getName().startsWith("additional_"))
                    .collect(Collectors.toList());

            for(Part p : additionalParts){
                int read = 0;
                final byte[] bytes = new byte[1024];
                InputStream filecontent = p.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((read = filecontent.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                //remove dynamically added "additional_" string from field name
                params.put(p.getName().substring("additional_".length()), new String(out.toByteArray(), Charset.defaultCharset()));
            }

            return params;
        } catch (Exception e) {
            logger.error("could not read additional parameters");
            e.printStackTrace();
            return null;
        }
    }

    public static Map<String, Object> getFilesFromRequest(HttpServletRequest request){
        Map<String, Object> params = new HashMap<>();

        try{
            List<Part> fileParts = request.getParts()
                    .stream()
                    .filter(part -> part.getSubmittedFileName() != null && !part.getName().equals("file"))
                    .collect(Collectors.toList());
            if(fileParts.size() > 0){
                for(Part p : fileParts){
                    int read = 0;
                    final byte[] bytes = new byte[1024];
                    InputStream filecontent = p.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    while ((read = filecontent.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                    params.put(p.getName(), out.toByteArray());
                }
            }

            return params;
        } catch (Exception e) {
            logger.error("could not read additional parameters");
            e.printStackTrace();
            return null;
        }
    }
}
