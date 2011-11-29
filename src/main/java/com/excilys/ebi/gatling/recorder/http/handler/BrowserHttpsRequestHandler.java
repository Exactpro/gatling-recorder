package com.excilys.ebi.gatling.recorder.http.handler;

import static com.excilys.ebi.gatling.recorder.http.channel.BootstrapFactory.getBootstrapFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.excilys.ebi.gatling.recorder.configuration.ProxyConfig;
import com.excilys.ebi.gatling.recorder.http.ssl.NextRequestEnabledSslHandler;
import com.excilys.ebi.gatling.recorder.http.ssl.SSLEngineFactory;

public class BrowserHttpsRequestHandler extends AbstractRequestHandler {

	private Map<String, Integer> securedHosts = new HashMap<String, Integer>();

	public BrowserHttpsRequestHandler(ProxyConfig proxyConfig) {
		super(proxyConfig.getHost(), proxyConfig.getPort());
	}

	@Override
	protected void requestReceived(ChannelHandlerContext ctx, final HttpRequest request) throws Exception {

		if (request.getMethod().equals(HttpMethod.CONNECT)) {
			URI uri = new URI("https://" + request.getUri());
			securedHosts.put(uri.getHost(), uri.getPort());

			ctx.getPipeline().addFirst("ssl", new NextRequestEnabledSslHandler(SSLEngineFactory.newServerSSLEngine()));
			ctx.getChannel().write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));

		} else {
			ClientBootstrap bootstrap = getBootstrapFactory().newClientBootstrap(ctx, request, true);

			ChannelFuture future;
			if (outgoingProxyHost == null) {
				String host = request.getHeader(HttpHeaders.Names.HOST);
				int port = securedHosts.get(host);
				future = bootstrap.connect(new InetSocketAddress(host, port));

			} else {
				future = bootstrap.connect(new InetSocketAddress(outgoingProxyHost, outgoingProxyPort));
			}

			future.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					future.getChannel().write(request);
				}
			});
		}
	}
}
