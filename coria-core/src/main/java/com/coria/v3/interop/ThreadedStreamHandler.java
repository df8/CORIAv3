package com.coria.v3.interop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Gross, 2017
 * ThreadedStreamHandler.java
 *
 * @version 0.1
 * <p>
 * This class is intended to be used with the SystemCommandExecutor
 * class to let users execute system commands from Java applications.
 * <p>
 * This class is based on work that was shared in a JavaWorld article
 * named "When System.exec() won't". That article is available at this
 * url:
 * <p>
 * http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
 * <p>
 * Copyright 2010 alvin j. alexander, devdaily.com.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Please ee the following page for the LGPL license:
 * http://www.gnu.org/licenses/lgpl.txt
 */
public class ThreadedStreamHandler extends Thread implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(ThreadedStreamHandler.class);

    final InputStream inputStream;
    String adminPassword;
    OutputStream outputStream;
    PrintWriter printWriter;
    final StringBuilder outputBuffer = new StringBuilder();
    private boolean sudoIsRequested = false;

    final List<String> messages = new ArrayList<>();

    /**
     * A simple constructor for when the sudo command is not necessary.
     * This constructor will just run the command you provide, without
     * running sudo before the command, and without expecting a password.
     *
     * @param inputStream
     */
    public ThreadedStreamHandler(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Use this constructor when you want to invoke the 'sudo' command.
     * The outputStream must not be null. If it is, you'll regret it. :)
     *
     * @param inputStream
     * @param outputStream
     * @param adminPassword
     */
    public ThreadedStreamHandler(InputStream inputStream, OutputStream outputStream, String adminPassword) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.printWriter = new PrintWriter(outputStream);
        this.adminPassword = adminPassword;
        this.sudoIsRequested = true;
    }

    public void run() {
        // on mac os x 10.5.x, when i run a 'sudo' command, i need to write
        // the admin password out immediately; that's why this code is
        // here.
        if (sudoIsRequested) {
            //doSleep(500);
            printWriter.println(adminPassword);
            printWriter.flush();
        }

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                logger.debug("PROC: {}", line);
                messages.add(line);
                outputBuffer.append(line).append("\n");
            }
        } catch (Throwable ioe) {
            ioe.printStackTrace();
        }
        // ignore this one
    }

    private void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public List<String> getMessages() {
        return messages;
    }

    public StringBuilder getOutputBuffer() {
        return outputBuffer;
    }

}
