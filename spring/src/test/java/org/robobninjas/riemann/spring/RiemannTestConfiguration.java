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

package org.robobninjas.riemann.spring;

import com.google.common.base.Throwables;
import org.robobninjas.riemann.json.RiemannEventObjectMapper;
import org.robobninjas.riemann.spring.server.RiemannProcess;
import org.robobninjas.riemann.spring.server.RiemannProcessConfiguration;
import org.robotninjas.riemann.client.RiemannTcpClient;
import org.robotninjas.riemann.client.RiemannTcpConnection;
import org.robotninjas.riemann.pubsub.QueryResultListener;
import org.robotninjas.riemann.pubsub.RiemannPubSubClient;
import org.robotninjas.riemann.pubsub.RiemannPubSubConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URISyntaxException;

/**
 * A spring configuration (factory) class used for tests using Riemann.
 * Starts Riemann server and connects to it with TCP and Websocket connection.
 *
 * @author Itai Frenkel
 * @since 3.0.1
 */
@Configuration
@Import({ RiemannProcessConfiguration.class,
          RiemannTcpClientConfiguration.class ,
          RiemannWebsocketClientConfiguration.class })
public class RiemannTestConfiguration {

    // We never use riemannProcess.
    // Injection makes sure Riemann server process is started, before connection attempts.
    @Inject
    RiemannProcess riemannProcess;

    @Value("${riemann.client.connection.timeout-milliseconds:1000}")
    int connectionTimeoutMilliseconds;


    @Value("${riemann.client.connection.number-of-connection-attempts:60}")
    int numberOfConnectionAttempts;

    @Value("${riemann.client.connection.sleep-before-connection-attempt-milliseconds:1000}")
    int sleepBeforeConnectionAttemptMilliseconds;

    @Value("${riemann.client.wait-for-tcp-server:true}")
    boolean waitForTcpServer;

    @Value("${riemann.client.wait-for-websocket-server:true}")
    boolean waitForWebSocketServer;

    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_TCP_PORT = RiemannTcpClient.DEFAULT_PORT;
    public static final int DEFAULT_WEBSOCKET_PORT = 5556;

    @Value("${riemann-client.host:" + DEFAULT_ADDRESS + "}")
    private String host;

    @Value("${riemann-client.tcp-port:"+ DEFAULT_TCP_PORT +"}")
    private Integer tcpPort;

    @Value("${riemann-client.websocket-port:"+ DEFAULT_WEBSOCKET_PORT +"}")
    private Integer webSocketPort;

    @PostConstruct
    public void waitForServers() {
        Exception lastException = null;

        for (int i = 1; i <= numberOfConnectionAttempts; i++) {
            try {
                checkTcpConnection();
                checkWebSocketConnection();
                return;
            } catch (Exception e) {
                lastException = e;
                if (i < numberOfConnectionAttempts) {
                    try {
                        Thread.sleep(sleepBeforeConnectionAttemptMilliseconds);
                    } catch (InterruptedException e1) {
                        throw Throwables.propagate(e1);
                    }
                }
            }
        }
        throw Throwables.propagate(lastException);
    }

    private void checkTcpConnection() throws InterruptedException, IOException {
        if (waitForTcpServer) {
            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(host, tcpPort), connectionTimeoutMilliseconds);
            clientSocket.close();
        }
    }

    private void checkWebSocketConnection() throws InterruptedException, IOException {
        if (waitForWebSocketServer) {
            Socket clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(host, webSocketPort), connectionTimeoutMilliseconds);
            clientSocket.close();
        }
    }

    @Bean
    RiemannEventObjectMapper riemannEventObjectMapper() {
        return new RiemannEventObjectMapper();
    }

    private QueryResultListener dummyListener() {
        return new QueryResultListener() {
            @Override
            public void handleResult(String result) {
                //Do nothing.
                //Test can add new listeners at will
            }
        };
    }
}
