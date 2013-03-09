package org.robotninjas.riemann.load;

import com.aphyr.riemann.Proto;
import com.google.common.base.Supplier;

public class DefaultEventSupplier implements Supplier<Proto.Event> {

  private final Proto.Event.Builder builder = Proto.Event.newBuilder();

  public DefaultEventSupplier() {
    builder.setMetricF(1000000).setService("thing");
  }

  @Override
  public Proto.Event get() {
    return builder.build();
  }
}
