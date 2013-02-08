package org.robobninjas.riemann;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.queue.BufferedWriteHandler;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class BlockingBufferedWriteHandler extends BufferedWriteHandler {

  private final AtomicLong bufferSize = new AtomicLong();
  private final int bufferedSize;
  private final ReentrantLock writeLock = new ReentrantLock();
  private final Condition writeableCondition = writeLock.newCondition();

  public BlockingBufferedWriteHandler(boolean consolidate, Queue<MessageEvent> eventQueue, int bufferedSize) {
    super(eventQueue, consolidate);
    this.bufferedSize = bufferedSize;
  }

  public BlockingBufferedWriteHandler(Queue<MessageEvent> eventQueue, int bufferedSize) {
    this(false, eventQueue, bufferedSize);
  }

  @Override public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    super.channelInterestChanged(ctx, e);
    try {
      writeLock.lock();
      if (e.getChannel().isWritable()) {
        writeableCondition.signalAll();
      }
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    super.writeRequested(ctx, e);

    final ChannelBuffer data = (ChannelBuffer) e.getMessage();
    final long newBufferSize = bufferSize.addAndGet(data.readableBytes());

    if (newBufferSize > bufferedSize) {
      try {
        writeLock.lock();
        while (!e.getChannel().isWritable()) {
          writeableCondition.await();
        }
      } finally {
        writeLock.unlock();
      }
      flush();
      bufferSize.set(0);
    }
  }


}
