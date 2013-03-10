package org.robotninjas.riemann.client;

import org.jboss.netty.channel.ChannelPipeline;

public class UdpClientPipelineFactory extends TcpClientPipelineFactory{
  @Override
  public ChannelPipeline getPipeline() throws Exception {
    final ChannelPipeline pipeline = super.getPipeline();
    pipeline.remove(TcpClientPipelineFactory.RETURNABLE_HANDLER);
    return pipeline;
  }
}
