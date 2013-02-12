package org.robobninjas.riemann;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioSocketChannelConfig;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingWriteHandler extends SimpleChannelHandler {

  private final AtomicLong bufferSize = new AtomicLong();
  private final ReentrantLock writeLock = new ReentrantLock();
  private final Condition writeable = writeLock.newCondition();

  @Override
  public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    try {
      writeLock.lock();
      if (e.getChannel().isWritable()) {
        bufferSize.set(0);
        writeable.signalAll();
      }
    } finally {
      writeLock.unlock();
    }
    super.channelInterestChanged(ctx, e);
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    super.writeRequested(ctx, e);

    final ChannelBuffer data = (ChannelBuffer) e.getMessage();

    try {
      writeLock.lock();
      while (!e.getChannel().isWritable()) {
        writeable.await();
      }
      bufferSize.set(0);
    } finally {
      writeLock.unlock();
    }
  }
}
