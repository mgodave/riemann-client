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

package org.robobninjas.riemann.spring.internal;

import com.google.common.base.Supplier;
import com.google.common.collect.Queues;
import org.jboss.netty.channel.MessageEvent;
import org.robotninjas.riemann.client.ReturnableMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

@Configuration
public class RiemannQueueConfiguration {

    //@Bean
    public Supplier<BlockingQueue<ReturnableMessage>> promiseQueueSupplier() {
        return new Supplier<BlockingQueue<ReturnableMessage>>() {
            @Override
            public BlockingQueue<ReturnableMessage> get() {
                return Queues.newArrayBlockingQueue(10000);
            }
        };
    }

    //@Bean
    public Queue<MessageEvent> sendBufferQueue() {
        return Queues.newConcurrentLinkedQueue();
    }

    //@Bean
    public BlockingQueue<ReturnableMessage> outstandingMessagesQueue() {
        return Queues.newArrayBlockingQueue(10000);
    }
}
