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
package com.excilys.ebi.gatling.recorder.configuration;

import java.util.List;

import com.excilys.ebi.gatling.recorder.ui.enumeration.FilterType;
import com.excilys.ebi.gatling.recorder.ui.enumeration.ResultType;

public class Configuration {

	private int port;
	private int sslPort;
	private ProxyConfig proxy = new ProxyConfig();
	private FilterType filterType;
	private List<Pattern> patterns;
	private String resultPath;
	private List<ResultType> resultTypes;
	private boolean saveConfiguration;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ProxyConfig getProxy() {
		return proxy;
	}

	public void setProxy(ProxyConfig proxy) {
		this.proxy = proxy;
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}

	public List<Pattern> getPatterns() {
		return patterns;
	}

	public void setPatterns(List<Pattern> patterns) {
		this.patterns = patterns;
	}

	public String getResultPath() {
		return resultPath;
	}

	public void setResultPath(String resultPath) {
		this.resultPath = resultPath;
	}

	public List<ResultType> getResultTypes() {
		return resultTypes;
	}

	public void setResultTypes(List<ResultType> resultTypes) {
		this.resultTypes = resultTypes;
	}

	public int getSslPort() {
		return sslPort;
	}

	public void setSslPort(int sslPort) {
		this.sslPort = sslPort;
	}

	public boolean isSaveConfiguration() {
		return saveConfiguration;
	}

	public void setSaveConfiguration(boolean saveConfiguration) {
		this.saveConfiguration = saveConfiguration;
	}

}
