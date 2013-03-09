package org.robotninjas.riemann.pubsub;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.jboss.netty.util.CharsetUtil;

class WebSocketClientHandler extends SimpleChannelHandler {

  private final WebSocketClientHandshaker handshaker;
  private final QueryResultListener listener;

  public WebSocketClientHandler(WebSocketClientHandshaker handshaker, QueryResultListener listener) {
    this.handshaker = handshaker;
    this.listener = listener;
  }

  @Override
  public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
    final ChannelFuture handshake = handshaker.handshake(e.getChannel());
    handshake.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isDone() && future.isSuccess()) {
          Channels.fireChannelConnected(ctx, e.getChannel().getRemoteAddress());
        }
      }
    });
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Channel ch = ctx.getChannel();
    if (!handshaker.isHandshakeComplete()) {
      handshaker.finishHandshake(ch, (HttpResponse) e.getMessage());
      return;
    }

    if (e.getMessage() instanceof HttpResponse) {
      HttpResponse response = (HttpResponse) e.getMessage();
      throw new Exception("Unexpected HttpResponse (status=" + response.getStatus() + ", content="
        + response.getContent().toString(CharsetUtil.UTF_8) + ')');
    }

    WebSocketFrame frame = (WebSocketFrame) e.getMessage();
    if (frame instanceof TextWebSocketFrame) {
      TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
      listener.handleResult(textFrame.getText());
    } else if (frame instanceof CloseWebSocketFrame) {
      ch.close();
    } else if (frame instanceof PingWebSocketFrame) {
      ch.write(new PongWebSocketFrame(frame.getBinaryData()));
    }
  }

}
