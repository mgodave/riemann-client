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
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.MessageOrBuilder;

abstract class ReturnableMessage<T> extends AbstractFuture<T> {

  private final Proto.Msg msg;

  public ReturnableMessage(Proto.Msg msg) {
    this.msg = msg;
  }

  public ReturnableMessage(Proto.Msg.Builder builder) {
    this(builder.build());
  }

  public Proto.Msg getMsg() {
    return msg;
  }

  public abstract void handleResult(Proto.Msg msg);

}
