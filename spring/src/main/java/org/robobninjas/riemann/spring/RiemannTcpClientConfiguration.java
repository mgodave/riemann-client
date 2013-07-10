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

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.robobninjas.riemann.spring.internal.RiemannConnectionPoolConfiguration;
import org.robobninjas.riemann.spring.internal.RiemannNioConfiguration;
import org.robobninjas.riemann.spring.internal.RiemannQueueConfiguration;
import org.robotninjas.riemann.client.ClientPipelineFactory;
import org.robotninjas.riemann.client.RiemannTcpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.InetSocketAddress;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A spring configuration class that
 * creates a new {@link RiemannTcpClient}.
 *
 * @author Itai Frenkel
 * @since 3.0.1
 */
@Configuration
@Import( {
    RiemannQueueConfiguration.class,
    RiemannNioConfiguration.class,
    RiemannConnectionPoolConfiguration.class})
public class RiemannTcpClientConfiguration {

    public static final String DEFAULT_ADDRESS = "localhost";
    public static final int DEFAULT_TCP_PORT = RiemannTcpClient.DEFAULT_PORT;

    @Value("${riemann-client.host:" + DEFAULT_ADDRESS + "}")
    private String host;

    @Value("${riemann-client.tcp-port:"+ DEFAULT_TCP_PORT +"}")
    private Integer port;

    @Inject
    private NioClientBossPool boss;

    @Inject
    private NioWorkerPool worker;

    @Autowired(required = false)
    ClientPipelineFactory pipelineFactory;

    @PostConstruct
    public void initDefaults() {

        checkArgument((port > 0) && (port < 65535), "Port number must be between 0 and 65535");

        if (pipelineFactory == null) {
            pipelineFactory = new ClientPipelineFactory();
        }
    }

    @Bean(destroyMethod = "shutdown")
    public RiemannTcpClient tcpClient() {
        final NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(boss, worker);
        final ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
        bootstrap.setPipelineFactory(pipelineFactory);
        bootstrap.setOption("remoteAddress", new InetSocketAddress(host, port));
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("child.tcpNoDelay", true);
        return new RiemannTcpClient(bootstrap);
    }
}
