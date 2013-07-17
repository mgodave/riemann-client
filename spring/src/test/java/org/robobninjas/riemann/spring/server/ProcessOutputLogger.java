/*******************************************************************************
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package org.robobninjas.riemann.spring.server;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pipes process output to the logger provided during instantiation using
 * a dedicated thread.
 *
 * @author Dan Kilman
 * @author Itai Frenkel
 * @since 0.1
 */
public class ProcessOutputLogger implements Runnable {

    private static final AtomicInteger ID = new AtomicInteger();

    private final BufferedReader processOutputReader;
    private final ExecutorService executorService;
    private final Logger logger;

    public ProcessOutputLogger(InputStream processInputStream , final Logger logger) {
        this.logger = logger;
        executorService =
            Executors.newFixedThreadPool(1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    final Thread thread = new Thread(r);
                    thread.setName("ProcessOutputLogger-" + logger.getName());
                    thread.setDaemon(true);
                    return thread;
                }
            });

        processOutputReader = new BufferedReader(new InputStreamReader(processInputStream));
        executorService.submit(this);
    }

    @Override
    public void run() {
        try {
            String line = processOutputReader.readLine();
            while (!Thread.interrupted() && line != null) {
                handleLine(line);
                line = processOutputReader.readLine();
            }
        } catch (IOException e) {
            logger.debug("Failed to read process " + ID);
        }
    }

    protected void handleLine(String line) {
        logger.info(line);
    }

    public void close() {
        executorService.shutdownNow();
    }
}