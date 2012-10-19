package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;

class UdpClientPipelineFactory implements ChannelPipelineFactory {

  public static int MAX_FRAME_SIZE = 16384;

  @Override
  public ChannelPipeline getPipeline() throws Exception {
    final ChannelPipeline pipeline = Channels.pipeline();
    pipeline.addLast("message-encoder", new ProtobufEncoder());
    pipeline.addLast("message-decoder", new ProtobufDecoder(Proto.Msg.getDefaultInstance()));
    pipeline.addLast("returnable-handler", new ReturnableHandler());
    pipeline.addLast("length-check", new UdpMessageLengthCheck());
    return pipeline;
  }

  private static class UdpMessageLengthCheck extends SimpleChannelDownstreamHandler {
    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      if (e.getMessage() instanceof ReturnableMessage) {
        final ReturnableMessage msg = (ReturnableMessage) e.getMessage();
        if (msg.getMsg().getSerializedSize() > MAX_FRAME_SIZE) {
          throw new TooLongFrameException("Riemann UDP frames must be <= " + MAX_FRAME_SIZE);
        }
      }
      super.writeRequested(ctx, e);
    }
  }
}
