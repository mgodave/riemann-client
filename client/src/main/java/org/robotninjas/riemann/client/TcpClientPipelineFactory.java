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
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedDownstreamThreadPoolExecutor;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TcpClientPipelineFactory implements ChannelPipelineFactory {

  private final Supplier<BlockingQueue<ReturnableMessage>> proimiseQueueSupplier;
  private final Supplier<Queue<MessageEvent>> sendBufferSupplier;

  @Inject
  public TcpClientPipelineFactory(Supplier<BlockingQueue<ReturnableMessage>> promiseQueueSupplier,
                                  Supplier<Queue<MessageEvent>> sendBufferSupplier) {
    this.proimiseQueueSupplier = promiseQueueSupplier;
    this.sendBufferSupplier = sendBufferSupplier;
  }

  public TcpClientPipelineFactory() {

    this.proimiseQueueSupplier = new Supplier<BlockingQueue<ReturnableMessage>>() {
      @Override
      public BlockingQueue<ReturnableMessage> get() {
        return Queues.newArrayBlockingQueue(1000);
      }
    };

    this.sendBufferSupplier = new Supplier<Queue<MessageEvent>>() {
      @Override
      public Queue<MessageEvent> get() {
        return Queues.newConcurrentLinkedQueue();
      }
    };

  }

  @Override
  public ChannelPipeline getPipeline() throws Exception {

    final ChannelPipeline pipeline = Channels.pipeline();

    final Meter throughput = Metrics.newMeter(getClass(), "throughput-meter", "bytes", TimeUnit.SECONDS);
    pipeline.addLast("throughput-meter", new SimpleChannelDownstreamHandler() {
      @Override
      public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        throughput.mark(((ChannelBuffer) e.getMessage()).readableBytes());
        super.writeRequested(ctx, e);
      }
    });

    pipeline.addLast("blocking-writer", new BlockingWriteHandler());

    pipeline.addLast("execution-handler", new ExecutionHandler(new OrderedDownstreamThreadPoolExecutor(1), true, false));
    pipeline.addLast("cached-writer", new CachedWriteHandler(new LinkedBlockingQueue<MessageEvent>(1000), true));

    pipeline.addLast("frame-encoder", new LengthFieldPrepender(4));
    pipeline.addLast("frame-decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));

    pipeline.addLast("message-encoder", new ProtobufEncoder());
    pipeline.addLast("message-decoder", new ProtobufDecoder(Proto.Msg.getDefaultInstance()));

    pipeline.addLast("returnable-handler", new ReturnableHandler(proimiseQueueSupplier.get()));

    return pipeline;
  }


}
