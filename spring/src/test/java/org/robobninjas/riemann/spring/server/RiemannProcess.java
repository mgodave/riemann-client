/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.robobninjas.riemann.spring.server;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Executes an external celery worker.
 * The process is closed with {@link #close()}
 *
 * @author Itai Frenkel
 * @since 3.0.1
 */
public class RiemannProcess implements AutoCloseable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RiemannProcessOutputLogger riemannOutputLogger;

    public RiemannProcess(final Path configPath) {
        String[] command = new String[] {"riemann", configPath.toString()};
        logger.debug("Starting Riemann with command : " + Arrays.toString(command));
        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(Lists.newArrayList(command));
        pb.redirectErrorStream(true);
        try {
            final Process process = pb.start();
            this.riemannOutputLogger = new RiemannProcessOutputLogger(process.getInputStream(), logger);
            if (!riemannOutputLogger.awaitOnline(1, TimeUnit.MINUTES)) {
                throw new IllegalStateException("Riemann failed to start TCP server");
            }
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    @Override
    public void close() throws Exception {

        logger.debug("Terminating riemann process.");
        String[] cmd;
        if (isWindows()) {
            cmd = new String[] {"cmd", "/c", "taskkill /F /PID " + getPid()};
        } else {
            cmd = new String [] {"kill", "-TERM", "" + getPid()};
        }

        final ProcessBuilder pb = new ProcessBuilder();
        pb.command(Lists.newArrayList(cmd));
        pb.redirectErrorStream(true);
        logger.debug("Terminating Riemann with command: " + Arrays.toString(cmd));
        final Process process = pb.start();
        final ProcessOutputLogger outputLogger = new ProcessOutputLogger(process.getInputStream(), logger);
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("Failed to terminate riemann process");
            }
        }
        finally {
            outputLogger.close();
        }
    }

    public String getHost() {
        return riemannOutputLogger.getHost();
    }

    public int getPid() {
        return riemannOutputLogger.getPid();
    }

    public Optional<Integer> getWebSocketPort() {
        return riemannOutputLogger.getWebSocketPort();
    }

    public Optional<Integer> getTcpPort() {
        return riemannOutputLogger.getTcpPort();
    }
}
