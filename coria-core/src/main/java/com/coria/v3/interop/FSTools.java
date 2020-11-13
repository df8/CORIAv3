package com.coria.v3.interop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Gross, 2017
 * Modified by David Fradin, 2020: Added ProcessExecutionResult
 */
@Component
public class FSTools {
    private final Logger logger = LoggerFactory.getLogger(FSTools.class);

    /**
     * Author: David Fradin, 2020
     */
    public static class ProcessExecutionResult {
        protected int exitCode;
        protected List<String> stdOutLines;
        protected List<String> errOutLines;

        public int getExitCode() {
            return exitCode;
        }

        public void setExitCode(int exitCode) {
            this.exitCode = exitCode;
        }

        public List<String> getStdOutLines() {
            return stdOutLines;
        }

        public void setStdOutLines(List<String> stdOutLines) {
            this.stdOutLines = stdOutLines;
        }

        public List<String> getErrOutLines() {
            return errOutLines;
        }

        public void setErrOutLines(List<String> errOutLines) {
            this.errOutLines = errOutLines;
        }
    }

    /**
     * Starts a new process on the host system and waits for it to finish.<br/>
     * After the execution, all information that was printed on the stdout by the new process will be logged
     * with the prefix PROCESS.<br/>
     * If there was an error while executing the process, it can be obtained by calling {@code getLastError()}
     *
     * @param params parameters which will be used to execute the process. Should contain full paths
     * @return execution status code. -1 if there was a problem
     */
    public ProcessExecutionResult startProcessAndWait(String params) {
        ThreadedStreamHandler stdOut;
        ThreadedStreamHandler errOut;

        ProcessExecutionResult result = new ProcessExecutionResult();
        try {

            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(params);

            stdOut = new ThreadedStreamHandler(pr.getInputStream());
            errOut = new ThreadedStreamHandler(pr.getErrorStream());

            stdOut.start();
            errOut.start();
            result.setExitCode(pr.waitFor());

            stdOut.interrupt();
            errOut.interrupt();
            stdOut.join();
            errOut.join();
            result.setStdOutLines(stdOut.getMessages());
            result.setErrOutLines(errOut.getMessages());
            if (result.getErrOutLines().size() > 0) {
                if (result.getExitCode() == 0) {
                    result.setExitCode(-1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception while starting process: {}", e.getMessage());
            if (result.getErrOutLines() == null) {
                result.setErrOutLines(new ArrayList<>());
            }
            result.getErrOutLines().add(String.format("Exception: %s", e.getMessage()));
            result.setExitCode(-1);
        }

        return result;
    }

    /**
     * starts a system process with given parameters and waits for it to finish
     *
     * @param params parameters to use for process execution
     * @return true if there are no errors
     */
    public ProcessExecutionResult startSyncSystemProcess(String params) {
        Instant starts = Instant.now();
        ProcessExecutionResult processResult = startProcessAndWait(params);

        Instant ends = Instant.now();
        logger.debug("process execution finished ({})", Duration.between(starts, ends));

        if (processResult.getExitCode() != 0) {
            //something happened -> do something
            logger.debug("process exit code: {}", processResult);
        }
        return processResult;

    }

    /**
     * Renames the response file of a process into keyword suffix "archive.csv" to indicate that the processing is finished.<br/>
     * If renaming fails, a deletion is attended.
     *
     * @param response File object linked to the response file
     */
    public void cleanupResponseFile(File response) {
        String archivedFileName = response.getName().replace("export.csv", "archive.csv");
        logger.debug("renaming response file to {}", archivedFileName);
        if (response.renameTo(new File(archivedFileName))) {
            logger.debug("successful renamed file");
        } else {
            logger.error("could not rename file - trying to delete it...");
            if (response.delete()) {
                logger.debug("response file was deleted");
            } else {
                logger.error("could not delete response file.\nNOTE: if the response file stays in coria working directory it will be reprocessed at restart of the application");
            }
        }
    }

    public boolean checkIfFileExists(String fileName) {
        if (fileName != null && fileName.trim().length() > 0) {
            File f = new File(fileName);
            return (f.isFile() && !f.isDirectory());
        }
        return false;
    }

    public boolean checkIfExecutableExists(String fileName) {
        if (fileName != null && fileName.trim().length() > 0) {
            File f = new File(fileName);
            return (f.isFile() && !f.isDirectory() && f.canExecute());//checks whether the executable exists and we have permission (UNIX) to execute.
        }
        return false;
    }

    public String escapePath(String str) {
        return str.replaceAll(" ", "\\\\ ");
    }
}
