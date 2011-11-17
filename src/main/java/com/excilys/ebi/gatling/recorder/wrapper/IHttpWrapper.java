package com.excilys.ebi.gatling.recorder.wrapper;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public interface IHttpWrapper {
	public HttpRequest getHttpRequest();

	public HttpResponse getHttpResponse();
}
