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

package org.robobninjas.riemann;

import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.jboss.netty.channel.Channels.write;

class ReturnableDownstreamHandler implements ChannelDownstreamHandler {

  private static final Logger logger = LoggerFactory.getLogger(ReturnableDownstreamHandler.class);
  private final BlockingQueue<ReturnableMessage<?>> returnables;

  public ReturnableDownstreamHandler(BlockingQueue<ReturnableMessage<?>> returnables) {
    this.returnables = returnables;
  }

  @Override
  public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    if (e instanceof MessageEvent) {
      final MessageEvent msgEvent = (MessageEvent) e;
      if (((MessageEvent) e).getMessage() instanceof ReturnableMessage<?>) {
        final ReturnableMessage<?> returnable = (ReturnableMessage<?>) msgEvent.getMessage();
        returnables.put(returnable);
        // strip the returnable and send the protobuf downstream
        write(ctx, e.getFuture(), returnable.getMsg(), ((MessageEvent) e).getRemoteAddress());
        return;
      }
    } else if (e instanceof ChannelStateEvent) {
      final ChannelStateEvent stateEvent = (ChannelStateEvent) e;
      if (stateEvent.getState() == ChannelState.OPEN && (Boolean) stateEvent.getValue() == false) {
        // channel was closed
        for (ReturnableMessage<?> returnable : returnables) {
          returnable.cancel(true);
        }
        returnables.clear();
      }
    }
    ctx.sendDownstream(e);
  }


}
