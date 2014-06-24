/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.recorder.http.handler

import java.net.{ InetSocketAddress, URI }

import org.jboss.netty.channel.{ Channel, ChannelFuture }
import org.jboss.netty.handler.codec.http.{ DefaultHttpResponse, HttpRequest, HttpResponseStatus, HttpVersion }

import io.gatling.recorder.http.HttpProxy
import io.gatling.recorder.http.channel.BootstrapFactory
import io.gatling.recorder.http.handler.ChannelFutures.function2ChannelFutureListener
import io.gatling.recorder.util.URIHelper

class ServerHttpRequestHandler(proxy: HttpProxy) extends ServerRequestHandler(proxy) {

  private def writeRequest(request: HttpRequest, clientChannel: Channel): Unit = {
    clientChannel.getPipeline.get(classOf[ClientHttpResponseHandler]).request = TimedHttpRequest(request)
    val relativeRequest = proxy.outgoingProxy match {
      case None => ServerRequestHandler.buildRequestWithRelativeURI(request)
      case _    => request
    }
    clientChannel.write(relativeRequest)
  }

  def propagateRequest(serverChannel: Channel, request: HttpRequest): Unit = {

    _clientChannel match {
      case Some(clientChannel) if clientChannel.isConnected && clientChannel.isOpen =>
        writeRequest(request, clientChannel)
      case _ =>
        _clientChannel = None

        val inetSocketAddress = proxy.outgoingProxy match {
          case Some((host, port)) => new InetSocketAddress(host, port)
          case _ =>
            try {
              // the URI sometimes contains invalid characters, so we truncate as we only need the host and port
              val (schemeHostPort, _) = URIHelper.splitURI(request.getUri)
              val uri = new URI(schemeHostPort)
              computeInetSocketAddress(uri)
            } catch {
              case e: Exception =>
                throw new RuntimeException(s"Could not build address requestURI='${request.getUri}', normalizedURI='${URIHelper.splitURI(request.getUri)._1}'", e)
            }
        }

        proxy.clientBootstrap
          .connect(inetSocketAddress)
          .addListener { future: ChannelFuture =>
            if (future.isSuccess) {
              val clientChannel = future.getChannel
              clientChannel.getPipeline.addLast(BootstrapFactory.GatlingHandlerName, new ClientHttpResponseHandler(proxy.controller, serverChannel, TimedHttpRequest(request), false))
              _clientChannel = Some(clientChannel)
              writeRequest(request, clientChannel)
            } else {
              val t = future.getCause
              logger.error(t.getMessage, t)
              // FIXME could be 404 or 500 depending on exception
              val response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)

              serverChannel.write(response)
            }
          }
    }
  }
}
