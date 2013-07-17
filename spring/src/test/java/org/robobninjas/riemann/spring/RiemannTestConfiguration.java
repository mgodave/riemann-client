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

import org.robobninjas.riemann.spring.server.RiemannProcess;
import org.robobninjas.riemann.spring.server.RiemannProcessConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * A spring configuration (factory) class used for tests using Riemann.
 * Starts Riemann server and connects to it with TCP and Websocket connection.
 *
 * @author Itai Frenkel
 * @since 3.0.1
 */
@Configuration
@Import({ RiemannProcessConfiguration.class,
          RiemannTestConfiguration.RiemannTcpClientTestConfiguration.class ,
          RiemannTestConfiguration.RiemannWebsocketClientTestConfiguration.class })
public class RiemannTestConfiguration {

    /**
     * Creates a tcp client autoconfigured with the Riemann server settings.
     *
     * @author Itai Frenkel
     * @since 0.1
     */
    @Configuration
    public static class RiemannTcpClientTestConfiguration extends RiemannTcpClientConfiguration {

        @Inject
        RiemannProcess riemannProcess;

        @PostConstruct
        @Override
        public void initDefaults() {
            super.setHost(riemannProcess.getHost());
            super.setPort(riemannProcess.getTcpPort().get());
            super.initDefaults();
        }
    }

    @Configuration
    public static class RiemannWebsocketClientTestConfiguration extends RiemannWebsocketClientConfiguration {

        @Inject
        RiemannProcess riemannProcess;

        @PostConstruct
        @Override
        public void initDefaults() {
            super.setHost(riemannProcess.getHost());
            super.setPort(riemannProcess.getWebSocketPort().get());
            super.initDefaults();
        }
    }
}
