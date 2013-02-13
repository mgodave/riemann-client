package org.robobninjas.riemann;

import com.sun.tools.internal.ws.wsdl.document.http.HTTPUrlReplacement;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.jboss.netty.util.CharsetUtil;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WebSocketClientHandler extends SimpleChannelHandler {

  private final WebSocketClientHandshaker handshaker;
  private final ReentrantLock handshakeLock = new ReentrantLock();
  private final AtomicBoolean isHandshakeComplete = new AtomicBoolean(false); // handshaker isHandshakeComplete does not use a volatile boolean
  private final Condition handshakeCompleteCondition = handshakeLock.newCondition();

  public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
    this.handshaker = handshaker;
  }

  @Override
  public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {

    final ChannelFuture handshakeFuture = handshaker.handshake(e.getChannel());

    handshakeFuture.addListener(new ChannelFutureListener() {
      @Override public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isDone() && future.isSuccess()) {
          Channels.fireChannelConnected(ctx, e.getChannel().getRemoteAddress());
        }
      }
    });
  }

  @Override public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

    if (e.getMessage() instanceof HttpRequest) {
      super.writeRequested(ctx, e);
      return;
    }

    if (!isHandshakeComplete.get()) {
      try {
        handshakeLock.lock();
        while (!isHandshakeComplete.get()) {
          handshakeCompleteCondition.await();
        }
      } finally {
        handshakeLock.unlock();
      }
    }

    final Channel channel = e.getChannel();
    final ChannelFuture future = e.getFuture();
    final ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
    final BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
    final SocketAddress address = e.getRemoteAddress();
    final DownstreamMessageEvent event = new DownstreamMessageEvent(channel, future, frame, address);

    super.writeRequested(ctx, event);
  }

  @Override public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

    final Channel ch = ctx.getChannel();
    if (!isHandshakeComplete.get()) {
      try {
        handshakeLock.lock();
        if (!handshaker.isHandshakeComplete()) {
          handshaker.finishHandshake(ch, (HttpResponse) e.getMessage());
          isHandshakeComplete.set(true);
          handshakeCompleteCondition.signalAll();
          return;
        }
      } finally {
        handshakeLock.unlock();
      }
    }

    if (e.getMessage() instanceof HttpResponse) {
      final HttpResponse response = (HttpResponse) e.getMessage();
      throw new Exception("Unexpected HttpResponse (status=" + response.getStatus() + ", content="
          + response.getContent().toString(CharsetUtil.UTF_8) + ')');
    }

    final WebSocketFrame frame = (WebSocketFrame) e.getMessage();
    if (frame instanceof BinaryWebSocketFrame) {
      final BinaryWebSocketFrame binFrame = (BinaryWebSocketFrame) frame;
      Channels.fireMessageReceived(ctx, binFrame.getBinaryData());
    } else if (frame instanceof PongWebSocketFrame) {
    } else if (frame instanceof CloseWebSocketFrame) {
      ch.close();
    } else if (frame instanceof PingWebSocketFrame) {
      ch.write(new PongWebSocketFrame(frame.getBinaryData()));
    }
  }

}
