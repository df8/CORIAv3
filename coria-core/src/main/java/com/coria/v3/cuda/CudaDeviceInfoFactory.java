package com.coria.v3.cuda;

import com.coria.v3.config.AppContext;
import com.coria.v3.interop.FSTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by David Fradin, 2020
 * Parses output by the deviceQuery utility by Nvidia and creates an instance of CudaDeviceInfo for each GPU (hardware) available
 * on the current system. The information is read from the outputs of the command line utility `deviceQuery`.
 * <p>
 * The source code of deviceQuery is automatically installed on Ubuntu together with the CUDA Toolkit, but it has to be manually compiled by running
 * "cd /usr/local/cuda/samples/1_Utilities/deviceQuery && make"
 */
@Component
public class CudaDeviceInfoFactory {
    protected final static Logger logger = LoggerFactory.getLogger(CudaDeviceInfoFactory.class);
    FSTools fsTools;

    @Autowired
    protected void setFsTools(FSTools fsTools) {
        this.fsTools = fsTools;
    }

    public List<? extends CudaDeviceInfo> getList() throws Exception {
        ArrayList<CudaDeviceInfo> result = new ArrayList<>();
        if (fsTools.checkIfExecutableExists(AppContext.getInstance().getCudaDeviceQueryPath())) {
            FSTools.ProcessExecutionResult deviceQueryResult = fsTools.startSyncSystemProcess(AppContext.getInstance().getCudaDeviceQueryPath());
            List<String> lines = deviceQueryResult.getStdOutLines();

            Pattern deviceIdLine = Pattern.compile("^Device (\\d): \"([^\"]+)\"");
            CudaDeviceInfo deviceInfo = null;
            StringBuilder b = new StringBuilder();
            for (int i = 6; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                Matcher m = deviceIdLine.matcher(line);
                if (m.find()) {
                    if (deviceInfo != null) {
                        deviceInfo.setDescription(b.toString());
                        result.add(deviceInfo);
                        b = new StringBuilder();
                    }
                    deviceInfo = new CudaDeviceInfo();
                    deviceInfo.setId(m.group(1));
                    deviceInfo.setName(m.group(2));
                } else if (deviceInfo != null) {
                    String[] attr = line.split(":\\s+", 2);
                    if (attr.length == 2) {
                        deviceInfo.addAttribute(attr[0].trim(), attr[1].trim());
                    } else if (attr.length == 1) {
                        attr = line.split("\\s{2,}", 2);
                        if (attr.length == 2) {
                            deviceInfo.addAttribute(attr[0].trim(), attr[1].trim());
                        } else if (line.equals("Compute Mode:")) {
                            b.append(line).append(System.lineSeparator());
                            i++;
                            deviceInfo.addAttribute("Compute Mode", lines.get(i).trim());
                        }
                    }
                    b.append(lines.get(i).trim()).append(System.lineSeparator());
                }
            }
            if (deviceInfo != null) {
                deviceInfo.setDescription(b.toString());
                result.add(deviceInfo);
            }
        } else {
            logger.error("deviceQuery utility executable is not available");
            // Check if there is a makefile.
            String cudaDeviceQueryPath = AppContext.getInstance().getCudaDeviceQueryPath();

            if (cudaDeviceQueryPath != null && fsTools.checkIfFileExists(cudaDeviceQueryPath.replace("deviceQuery/deviceQuery", "deviceQuery/Makefile"))) {
                logger.error("Makefile for deviceQuery found. Please open a terminal and run `sudo make` in {}", AppContext.getInstance().getCudaDeviceQueryPath().replace("deviceQuery/deviceQuery", "deviceQuery/"));
            }
            String message = "WARNING: GPU-accelerated metrics will not work. Reason: The NVIDIA CUDA Utility \"deviceQuery\" was not found. " +
                    "Please make sure the newest version of CUDA is installed on your system and compile the 'deviceQuery' executable by running 'make' " +
                    "in the '<cuda_dir>/samples/1_Utilities/deviceQuery/' directory.";
            logger.error(message);
            throw new Exception(message);
        }

        return result;
    }

}
