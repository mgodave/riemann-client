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

import com.google.common.base.Supplier;
import com.google.common.util.concurrent.MoreExecutors;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.robobninjas.riemann.json.RiemannEventObjectMapper;
import org.robobninjas.riemann.spring.internal.RiemannNioConfiguration;
import org.robotninjas.riemann.pubsub.RiemannPubSubClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * A spring configuration class that
 * creates a new {@link org.robotninjas.riemann.client.RiemannTcpClient}.
 *
 * @author Itai Frenkel
 * @since 3.0.1
 */
@Configuration
@Import({RiemannNioConfiguration.class })
public class RiemannWebsocketClientConfiguration {
    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_PORT = 5556;

    @Value("${riemann-client.host:" + DEFAULT_ADDRESS + "}")
    private String host;

    protected void setHost(String host) {
        this.host = host;
    }

    @Value("${riemann-client.websocket-port:"+ DEFAULT_PORT +"}")
    private int port;
    protected void setPort(int port) {
        this.port = port;
    }

    @Inject
    private NioClientBossPool boss;

    @Inject
    private NioWorkerPool worker;

    @PostConstruct
    public void initDefaults() {
        //place holder for subclasses
    }

    @Bean
    public RiemannPubSubClient pubsubClient() {
        final NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(boss, worker);
        return new RiemannPubSubClient(
                host, port,
                new WebSocketClientHandshakerFactory(),
                new Supplier<ClientBootstrap>() {
                    @Override
                    public ClientBootstrap get() {
                        return new ClientBootstrap(channelFactory);
                    }
                },
                MoreExecutors.sameThreadExecutor());
    }

    @Bean
    RiemannEventObjectMapper riemannEventObjectMapper() {
        return new RiemannEventObjectMapper();
    }
}
