/*

 Copyright 2012 David Rusek

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

*/

package org.robotninjas.riemann.client;

import com.aphyr.riemann.Proto;
import com.google.common.base.Supplier;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;

import java.util.Queue;

public class TcpClientPipelineFactory implements ChannelPipelineFactory {

  private final Supplier<Queue<ReturnableMessage>> proimiseQueueSupplier;
  private final Supplier<Queue<MessageEvent>> messageQueueSupplier;
  private final int bufferSize;

  @Inject
  public TcpClientPipelineFactory(Supplier<Queue<ReturnableMessage>> promiseQueueSupplier,
                                  Supplier<Queue<MessageEvent>> messageQueueSupplier,
                                  @BufferSize int bufferSize) {
    this.proimiseQueueSupplier = promiseQueueSupplier;
    this.messageQueueSupplier = messageQueueSupplier;
    this.bufferSize = bufferSize;
  }

  public TcpClientPipelineFactory(@BufferSize int bufferSize) {

    this.proimiseQueueSupplier = new Supplier<Queue<ReturnableMessage>>() {
      @Override public Queue<ReturnableMessage> get() {
        return Queues.newConcurrentLinkedQueue();
      }
    };

    this.messageQueueSupplier = new

        Supplier<Queue<MessageEvent>>() {
          @Override public Queue<MessageEvent> get() {
            return Queues.newConcurrentLinkedQueue();
          }
        };

    this.bufferSize = bufferSize;
  }

  @Override
  public ChannelPipeline getPipeline() throws Exception {

    final ChannelPipeline pipeline = Channels.pipeline();

    //pipeline.addLast("auto-flusher", new BlockingBufferedWriteHandler(messageQueueSupplier.get(), bufferSize));
    pipeline.addLast("blocking-writer", new BlockingWriteHandler());

    //final OrderedDownstreamThreadPoolExecutor executor = new OrderedDownstreamThreadPoolExecutor(2);
    //pipeline.addLast("execution-handler", new ExecutionHandler(executor, true, false));

    pipeline.addLast("frame-encoder", new LengthFieldPrepender(4));
    pipeline.addLast("frame-decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));

    pipeline.addLast("message-encoder", new ProtobufEncoder());
    pipeline.addLast("message-decoder", new ProtobufDecoder(Proto.Msg.getDefaultInstance()));

    pipeline.addLast("returnable-handler", new ReturnableHandler(proimiseQueueSupplier.get()));

    return pipeline;
  }


}
