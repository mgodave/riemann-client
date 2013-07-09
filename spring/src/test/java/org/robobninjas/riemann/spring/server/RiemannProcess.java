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

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Executes an external celery worker.
 * The process is closed with {@link #close()}
 *
 * @author Itai Frenkel
 * @since 3.0.1
 */
public class RiemannProcess implements AutoCloseable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public RiemannProcess(final Path configPath) {
        String[] command = new String[] {"riemann", configPath.toString()};
        runProcess(command);
    }


    private Process runProcess(String[] command) {
        logger.debug("running command : " + Arrays.toString(command));
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(Lists.newArrayList(command));
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            return pb.start();
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    @Override
    public void close() throws Exception {

        Process p;
        logger.debug("Terminating riemann process.");
        if (isWindows()) {
            throw new UnsupportedOperationException("Killing riemann on windows is still not supported");
        } else {
            p = runProcess(new String [] {"/bin/sh", "-c", "kill $(ps aux | grep '[r]iemann.jar' | awk '{print $2}')" +
                    ""});
        }
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Failed to terminate riemann process");
        }
    }
}
