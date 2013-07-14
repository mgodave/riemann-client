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

import com.aphyr.riemann.Proto;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Queues;
import org.robobninjas.riemann.json.RiemannEventObjectMapper;
import org.robobninjas.riemann.spring.server.RiemannProcess;
import org.robobninjas.riemann.spring.server.RiemannProcessConfiguration;
import org.robotninjas.riemann.client.RiemannTcpClient;
import org.robotninjas.riemann.client.RiemannTcpConnection;
import org.robotninjas.riemann.pubsub.QueryResultListener;
import org.robotninjas.riemann.pubsub.RiemannPubSubClient;
import org.robotninjas.riemann.pubsub.RiemannPubSubConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests Riemann tcp tcpClient.
 *
 * @author Itai Frenkel
 * @since 3.0.1
 */
@ContextConfiguration(classes = { RiemannTest.Config.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RiemannTest extends AbstractTestNGSpringContextTests implements QueryResultListener {

    private BlockingQueue<String> events = Queues.newArrayBlockingQueue(10);

    @Override
    public void handleResult(String result) {
        events.add(result);
    }

    /**
     */
    @Configuration
    @PropertySource("org/robobninjas/riemann/spring/riemann-test.properties")
    @Import({ RiemannTestConfiguration.class })
    static class Config {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    // component being tested
    @Inject
    private RiemannProcess riemannProcess;

    @Inject
    private RiemannTcpClient tcpClient;
    private RiemannTcpConnection tcpConnection;

    @Inject
    private RiemannPubSubClient pubSubClient;
    private RiemannPubSubConnection pubSubConnection;

    @PostConstruct
    public void establishConnection() {
       tcpConnection = makeConnection();
       pubSubConnection = continuousQuery();
    }

    @PreDestroy
    public void closeConnection() throws IOException {
        if (tcpConnection != null) {
            tcpConnection.close();
        }
        if (pubSubConnection != null) {
            pubSubConnection.close();
        }
    }

    @Test(timeOut = 60000)
    public void testSendWithAck() throws InterruptedException {

        boolean success = sendWithAck();
        assertThat(success).isEqualTo(Boolean.TRUE);
    }

    @Test(dependsOnMethods = {"testSendWithAck" }, timeOut = 60000)
    public void testQuery() throws InterruptedException {
        List<Proto.Event> events = query();
        assertThat(Iterables.getOnlyElement(events).getMetricD()).isEqualTo(5.3);
    }

    @Test(dependsOnMethods = {"testSendWithAck" }, timeOut = 60000)
    public void testContinuousQuery() throws InterruptedException, IOException {
        String json = events.take();
        RiemannEventObjectMapper mapper = new RiemannEventObjectMapper();
        assertThat(mapper.readEvent(json).getMetricD()).isEqualTo(5.3);
    }

    private List<Proto.Event> query() throws InterruptedException {
        try {
            return tcpConnection.query(queryString()).get();

        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    private RiemannTcpConnection makeConnection() {
        try {
            return tcpClient.makeConnection();
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }

    private boolean sendWithAck() {
        final Future<Boolean> isOk =
                tcpConnection.sendWithAck(createEvent());

        try {
            return isOk.get();
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    private Proto.Event createEvent() {
        return Proto.Event
                .newBuilder()
                .setService("fridge")
                .setState("running")
                .setMetricD(5.3)
                .addTags("appliance")
                .addTags("cold")
                .build();
    }

    private String queryString() {
        return "tagged \"cold\" and metric > 0";
    }

    private RiemannPubSubConnection continuousQuery() {
        try {
            return this.pubSubClient.makeConnection(queryString(), true, this);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }
}
