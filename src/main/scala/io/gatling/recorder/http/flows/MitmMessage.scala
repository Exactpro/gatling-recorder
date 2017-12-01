/*
 * Copyright 2011-2017 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.recorder.http.flows

import io.netty.channel.{ Channel, ChannelId }
import io.netty.handler.codec.http.{ FullHttpRequest, FullHttpResponse }

sealed trait MitmMessage

object MitmMessage {

  case object ServerChannelInactive extends MitmMessage

  // TODO support not using ObjectAggregator?
  case class RequestReceived(request: FullHttpRequest) extends MitmMessage

  case class ClientChannelActive(channel: Channel) extends MitmMessage

  case class ClientChannelException(t: Throwable) extends MitmMessage

  case class ClientChannelInactive(channelId: ChannelId) extends MitmMessage

  case class ResponseReceived(response: FullHttpResponse) extends MitmMessage
}
