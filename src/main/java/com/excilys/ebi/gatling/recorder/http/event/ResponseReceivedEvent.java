/**
 * Copyright 2011 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.excilys.ebi.gatling.recorder.http.event;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class ResponseReceivedEvent {

	private final HttpRequest request;
	private final HttpResponse response;
	private int id;
	private Map<String, List<String>> requestParams = new LinkedHashMap<String, List<String>>();
	private boolean withBody;
	private boolean withUrlBase;

	public ResponseReceivedEvent(HttpRequest request, HttpResponse response) {
		this.request = request;
		this.response = response;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map<String, List<String>> getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(Map<String, List<String>> requestParams) {
		this.requestParams = requestParams;
	}

	public boolean isWithBody() {
		return withBody;
	}

	public void setWithBody(boolean withBody) {
		this.withBody = withBody;
	}

	public boolean isWithUrlBase() {
		return withUrlBase;
	}

	public void setWithUrlBase(boolean withUrlBase) {
		this.withUrlBase = withUrlBase;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public HttpResponse getResponse() {
		return response;
	}
}
