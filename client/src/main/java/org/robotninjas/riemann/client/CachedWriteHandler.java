package org.robotninjas.riemann.client;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class CachedWriteHandler extends BlockingBufferedWriteHandler {

  private final ReentrantLock writeableLock = new ReentrantLock();

  public CachedWriteHandler() {
  }

  public CachedWriteHandler(BlockingQueue<MessageEvent> queue) {
    super(queue);
  }

  public CachedWriteHandler(boolean consolidateOnFlush) {
    super(consolidateOnFlush);
  }

  public CachedWriteHandler(BlockingQueue<MessageEvent> queue, boolean consolidateOnFlush) {
    super(queue, consolidateOnFlush);
  }

  @Override
  public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    try {
      writeableLock.lock();
      if (e.getChannel().isWritable()) {
        flush();
      }
    } finally {
      writeableLock.unlock();
    }
    super.channelInterestChanged(ctx, e);
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    super.writeRequested(ctx, e);
    try {
      writeableLock.lock();
      if (e.getChannel().isWritable()) {
        flush();
      }
    } finally {
      writeableLock.unlock();
    }
  }
}
