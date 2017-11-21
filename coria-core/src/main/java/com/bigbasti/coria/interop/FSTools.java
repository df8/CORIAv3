package com.bigbasti.coria.interop;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Sebastian Gross
 */
@Component
public class FSTools {
    private Logger logger = LoggerFactory.getLogger(FSTools.class);

    @Autowired
    Environment env;

    /**
     * saves the last error that occurred while executing a process.
     * This information is useful to display to the user why the process failed
     */
    String lastError = "";

    /**
     * Tries to determine the CORIA working directory.<br/>
     * First the <code>application.properties</code> setting <code>coria.workingwidectory</code> will be checked,
     * if it does not exist the system envorionment property <code>CORIA_HOME</code> will be read. If this property
     * also does not yield a result an exception will be thrown.
     * @return full path to the CORIA working directory of an exception
     * @throws Exception if the working directory can not be found
     */
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

    /**
     * Starts a new process on the host system and waits for it to finish.<br/>
     * After the execution, all information that was printed on the stdout by the new process will be logged
     * with the prefix PROCESS.<br/>
     * If there was an error while executing the process, it can be obtained by calling {@code getLastError()}
     * @param params parameters which will be used to execute the process. Should contain full paths
     * @return execution status code. -1 if there was a problem
     */
    public int startProcessAndWait(String params){
        ThreadedStreamHandler stdOut;
        ThreadedStreamHandler errOut;
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(params);

            stdOut = new ThreadedStreamHandler(pr.getInputStream());
            errOut = new ThreadedStreamHandler(pr.getErrorStream());

            stdOut.start();
            errOut.start();

            int exitValue = pr.waitFor();

            stdOut.interrupt();
            errOut.interrupt();
            stdOut.join();
            errOut.join();

            lastError = errOut.getMessages().get(errOut.getMessages().size()-1);

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

    /**
     * creates a file inside the CORIA working directory containing all edges of a dataset for further processing
     * @param edges the edges to export
     * @param filePath path to create the file at
     * @param separator separator string to use between values
     * @param prefix string value to attach in front of each line
     * @return true if everything goes well
     */
    public boolean writeEdgesFileToWorkingDir(List<CoriaEdge> edges, String filePath, String separator, String prefix){
        Instant starts = Instant.now();
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filePath));
            for(CoriaEdge e : edges){
                pw.println(prefix + e.getSourceNode() + separator + e.getDestinationNode());
            }
            pw.close();

            Instant ends = Instant.now();
            logger.debug("python import file created succellful ({})", Duration.between(starts, ends));
            return true;
        } catch (Exception e) {
            logger.error("could not create python import file: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * starts a system process with given parameters and waits for it to finish
     * @param params parameters to use for process execution
     * @return true if there are no errors
     */
    public boolean startSyncSystemProcess(String params){
        Instant starts = Instant.now();
        try {
            int exitValue = startProcessAndWait(params);

            Instant ends = Instant.now();
            logger.debug("process execution finished ({})", Duration.between(starts, ends));

            if (exitValue != 0) {
                //something happened -> do something
                logger.debug("process exit code: {}", exitValue);
            }

            return true;
        }
        catch(Exception e) {
            logger.error("error while starting resource: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Tries to find the given local resource and extract the full system path to it
     * @param resource resource to look for
     * @return full system path to the resource or ""
     */
    public String getFullPathToResource(String resource){
        URL dir_url = getClass().getResource(resource);
        try {
            return Paths.get(dir_url.toURI()).toFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            logger.error("could not locate resource: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Reads a response file from an executed NetworkX script and attaches the results to a dataset
     * @param dsToUpdate dataset to attach the results to
     * @param responseFile file containing the script response
     * @param paramSeparator separation string used to separate values in a line
     * @param metricShortcut shortcut of the metric which created the file
     * @param metricName name of the metric which created the file
     * @return dataset with updated nodes
     */
    public DataSet readNetworkXMetricResponseAndUpdateDataset(DataSet dsToUpdate, File responseFile, String paramSeparator, String metricShortcut, String metricName){
        logger.debug("reading response and updating dataset ...");
        Instant starts = Instant.now();

        BufferedReader br = null;
        double maxMetricVal = 0.0;
        List<CoriaNode> nodes = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(responseFile));
            for (String line; (line = br.readLine()) != null; ) {
                String parts[] = line.split(paramSeparator);
                Optional<CoriaNode> ocn = dsToUpdate.getNodes().stream().filter(coriaNode -> coriaNode.getAsid().equals(parts[0])).findFirst();
                if(!ocn.isPresent()){
                    logger.warn("could not update node {} (value:{}) - node not found in dataset", parts[0], parts[1]);
                }else{
                    CoriaNode cn = ocn.get();
                    cn.setAttribute(metricShortcut, parts[1]);
                    nodes.add(cn);
                    try{
                        Double metricVal = Double.valueOf(parts[1]);
                        if(metricVal > maxMetricVal){
                            maxMetricVal = metricVal;
                        }
                    }catch(Exception ex){logger.warn("could not parse {} value {} from node {}",metricShortcut, parts[1], parts[0]);}
                }
            }
            br.close();
        } catch (IOException e) {
            logger.error("failed reading response file: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("failed reading response file: " + e.getMessage());
        }

        logger.debug("updating relative {}", metricName);

        for(CoriaNode n : nodes){
            Double relMetricVal = (Double.valueOf(n.getAttribute(metricShortcut)) / maxMetricVal) * 100;
            logger.trace("{}: {} / {} * 100 = {}", metricShortcut, Double.valueOf(n.getAttribute(metricShortcut)), maxMetricVal, relMetricVal);
            n.setAttribute(metricShortcut+"_relative", relMetricVal.toString());
        }

        Instant ends = Instant.now();
        logger.debug("finished reading response file ({})", Duration.between(starts, ends));
        return dsToUpdate;
    }

    public DataSet readNetworkXLayoutResponseAndUpdateDataset(DataSet dsToUpdate, File responseFile, String paramSeparator, String metricShortcut){
        logger.debug("reading response and updating dataset ...");
        Instant starts = Instant.now();

        BufferedReader br = null;
        List<CoriaNode> nodes = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(responseFile));
            for (String line; (line = br.readLine()) != null; ) {
                String parts[] = line.split(paramSeparator);
                Optional<CoriaNode> ocn = dsToUpdate.getNodes().stream().filter(coriaNode -> coriaNode.getAsid().equals(parts[0])).findFirst();
                if(!ocn.isPresent()){
                    logger.warn("could not update node {} (value:{}) - node not found in dataset", parts[0], parts[1]);
                }else{
                    CoriaNode cn = ocn.get();
                    String coordinates = parts[1];
                    //networkx sometimes formats numbers in a strange way, we need to correct those
                    coordinates = coordinates.replaceAll("0\\. ", "0.0");
                    coordinates = coordinates.replaceAll("1\\. ", "1.0");
                    coordinates = coordinates.replace("[","").replace("]","").trim().replaceAll("[ ]+", ":");
                    if(coordinates.equals("1.:0.")){coordinates = "1.0:0.0";}
                    cn.setAttribute(metricShortcut, coordinates);
                    nodes.add(cn);
                }
            }
            br.close();
        } catch (IOException e) {
            logger.error("failed reading response file: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("failed reading response file: " + e.getMessage());
        }

        Instant ends = Instant.now();
        logger.debug("finished reading response file ({})", Duration.between(starts, ends));
        return dsToUpdate;
    }

    /**
     * Renames the response file of a process to end with archive to indicate, that the processing of it was finished.<br/>
     * If renaming fails, a deletion is attended.
     * @param responseFileName the used name to generate the response file
     * @param response File object linked to the response file
     */
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

    public String getLastError() {
        return lastError;
    }
}
