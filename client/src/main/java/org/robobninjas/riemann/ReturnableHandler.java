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

import com.aphyr.riemann.Proto;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.jboss.netty.channel.Channels.write;

class ReturnableHandler implements ChannelUpstreamHandler, ChannelDownstreamHandler {

  private static final Logger logger = LoggerFactory.getLogger(ReturnableHandler.class);

  private final ConcurrentLinkedQueue<WeakReference<ReturnableMessage<?>>> returnables =
    new ConcurrentLinkedQueue<WeakReference<ReturnableMessage<?>>>();

  @Override
  public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    if (e instanceof MessageEvent) {
      final MessageEvent msgEvent = (MessageEvent) e;
      if (((MessageEvent) e).getMessage() instanceof ReturnableMessage<?>) {
        final ReturnableMessage<?> returnable = (ReturnableMessage<?>) msgEvent.getMessage();
        returnables.offer(new WeakReference<ReturnableMessage<?>>(returnable));
        // strip the returnable and send the protobuf downstream
        write(ctx, e.getFuture(), returnable.getMsg(), ((MessageEvent) e).getRemoteAddress());
        return;
      }
    } else if (e instanceof ChannelStateEvent) {
      final ChannelStateEvent stateEvent = (ChannelStateEvent) e;
      if (stateEvent.getState() == ChannelState.OPEN && (Boolean) stateEvent.getValue() == false) {
        // channel was closed
        for (WeakReference<ReturnableMessage<?>> returnableReference : returnables) {
          final ReturnableMessage returnable = returnableReference.get();
          if (returnable != null) {
            returnable.cancel(true);
          }
        }
        returnables.clear();
      }
    }
    ctx.sendDownstream(e);
  }

  @Override
  public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    if (e instanceof MessageEvent) {
      final MessageEvent msgEvent = (MessageEvent) e;
      final WeakReference<ReturnableMessage<?>> returnableReference = returnables.poll();
      final ReturnableMessage returnable = returnableReference.get();
      if (returnable != null) {
        final Proto.Msg msg = (Proto.Msg) msgEvent.getMessage();
        returnable.handleResult(msg);
      }
    }
    ctx.sendUpstream(e);
  }

}
