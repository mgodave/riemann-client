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

import com.google.common.base.Optional;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Start Detector for Riemann process, and also discovers port settings.
 * Uses stdout parsing.
 *
 * @author Itai Frenkel
 * @since 0.1
 */
public class RiemannProcessOutputLogger extends ProcessOutputLogger {


    private static final Pattern PID_PATTERN = Pattern.compile(".*main - riemann.bin - PID (\\d+)");
    private static final Pattern TCP_PATTERN = Pattern.compile(
            ".*riemann.transport.tcp - TCP server (.*) (.*) online.*");
    private static final Pattern ONLINE_PATTERN = Pattern.compile(".*" + Pattern.quote(
            "main - riemann.core - Hyperspace core online") +".*");

    private static final Pattern WEBSOCKET_PATTERN = Pattern.compile(
            ".*riemann.transport.websockets - Websockets server (.*) (.*) online.*");

    //Pattern udp = Pattern.compile("riemann.transport.udp - UDP server 127.0.0.1 5555 16384 online");

    private String tcpIpAddress = null;
    private int pid;
    private final CountDownLatch latch;
    private boolean scan;
    private String host;
    private Optional<Integer> webSocketPort = Optional.absent();
    private Optional<Integer> tcpPort = Optional.absent();

    public RiemannProcessOutputLogger(
            InputStream processInputStream,
            Logger logger) {
        super(processInputStream, logger);
        scan = true;
        latch = new CountDownLatch(1);
    }

    @Override
    protected void handleLine(String line) {
        super.handleLine(line);
        if (scan) {
            scanPid(line);
            scanTcp(line);
            scanWebSocket(line);
            scanOnline(line);
        }
    }

    private void scanPid(String line) {
        final Matcher pidMatcher = PID_PATTERN.matcher(line);
        if (pidMatcher.matches()) {
            pid = Integer.valueOf(pidMatcher.group(1));
        }
    }

    private void scanTcp(String line) {
        final Matcher matcher = TCP_PATTERN.matcher(line);
        if (matcher.matches()) {
            if (host == null) {
                host = matcher.group(1);
            }
            tcpPort = Optional.fromNullable(Integer.valueOf(matcher.group(2)));
        }
    }

    private void scanWebSocket(String line) {
        final Matcher matcher = WEBSOCKET_PATTERN.matcher(line);
        if (matcher.matches()) {
            if (host == null) {
                host = matcher.group(1);
            }
            webSocketPort = Optional.fromNullable(Integer.valueOf(matcher.group(2)));
        }
    }

    private void scanOnline(String line) {
        final Matcher onlineMatcher = ONLINE_PATTERN.matcher(line);
        if (onlineMatcher.matches()) {
            scan = false;
            latch.countDown();
        }
    }

    public int getPid() {
        return pid;
    }

    public boolean awaitOnline(long timeout, TimeUnit unit) throws InterruptedException {
        return latch.await(timeout, unit);
    }

    public String getHost() {
        return host;
    }

    public Optional<Integer> getTcpPort() {
        return tcpPort;
    }

    public Optional<Integer> getWebSocketPort() {
        return webSocketPort;
    }
}
