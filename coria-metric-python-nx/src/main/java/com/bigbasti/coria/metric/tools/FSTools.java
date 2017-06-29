package com.bigbasti.coria.metric.tools;

import com.bigbasti.coria.interop.ThreadedStreamHandler;
import com.bigbasti.coria.metric.nx.NXBetweennessCentrality;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Created by Sebastian Gross
 */
@Component
public class FSTools {
    private Logger logger = LoggerFactory.getLogger(FSTools.class);

    @Autowired
    Environment env;

    public String getWorkingDirectory() throws Exception {
        logger.debug("determining coria working directory...");
        String workingDir = env.getProperty("coria.workingdirectory");
        if(Strings.isNullOrEmpty(workingDir)){
            logger.debug("could not load working directory from properties, trying system variable 'CORIA_HOME'...");
            workingDir = System.getenv("CORIA_HOME");
            if(Strings.isNullOrEmpty(workingDir)){
                logger.error("could not determine working directory");
                throw new Exception("could not determine working directory");
            }
        }
        logger.debug("using following directory as coria home: {}", workingDir);
        return workingDir;
    }

    public int startProcessAndWait(String params){
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(params);

            ThreadedStreamHandler stdOut = new ThreadedStreamHandler(pr.getInputStream());
            ThreadedStreamHandler errOut = new ThreadedStreamHandler(pr.getErrorStream());

            stdOut.start();
            errOut.start();

            int exitValue = pr.waitFor();

            stdOut.interrupt();
            errOut.interrupt();
            stdOut.join();
            errOut.join();

            return exitValue;
        } catch (IOException e) {
            logger.error("IO Exception while starting process: {}", e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            logger.error("InterruptedException while starting process: {}", e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public void cleanupResponseFile(String responseFileName, File response){
        String archivedFileName = responseFileName.replace("export.csv", "archive.csv");
        logger.debug("renaming response file to {}", archivedFileName);
        if(response.renameTo(new File(archivedFileName))){
            logger.debug("succellful renamed file");
        }else{
            logger.error("could not rename file - trying to delete it...");
            if(response.delete()){
                logger.debug("response file was deleted");
            }else{
                logger.error("could not delete response file.\nNOTE: if the response file stays in coria working directory it will be reprocessed at restart of the application");
            }
        }
    }
}
