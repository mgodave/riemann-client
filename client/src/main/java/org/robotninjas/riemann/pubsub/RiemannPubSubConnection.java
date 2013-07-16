package org.robotninjas.riemann.pubsub;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

public class RiemannPubSubConnection implements QueryResultListener {

  private final CopyOnWriteArrayList<QueryResultListener> listeners;
  private final Executor resultExecutor;
  private final ClientBootstrap bootstrap;
  private Optional<Channel> channel = Optional.absent();

  public RiemannPubSubConnection(Executor resultExecutor, QueryResultListener listener, ClientBootstrap bootstrap) {
    this.listeners = Lists.newCopyOnWriteArrayList();
    this.resultExecutor = resultExecutor;
    this.bootstrap = bootstrap;
    addQueryListener(listener);
  }

  public void init(Channel channel) {
    this.channel = Optional.of(channel);
  }

  @Override
  public void handleResult(final String result) {
    resultExecutor.execute(new Runnable() {
      @Override
      public void run() {
        for (QueryResultListener listener : listeners) {
          listener.handleResult(result);
        }
      }
    });
  }

  public void addQueryListener(QueryResultListener listener) {
    listeners.add(listener);
  }

  public void removeQueryListener(QueryResultListener listener) {
    listeners.remove(listener);
  }

  public void close() throws InterruptedException {
    try {
      if (channel.isPresent()) {
        channel.get().close().await();
      }
    }
    finally {
      bootstrap.releaseExternalResources();
    }
  }
}
