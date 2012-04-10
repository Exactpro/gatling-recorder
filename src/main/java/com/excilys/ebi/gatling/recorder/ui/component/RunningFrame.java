/**
 * Copyright 2011-2012 eBusiness Information, Groupe Excilys (www.excilys.com)
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
package com.excilys.ebi.gatling.recorder.ui.component;

import static com.excilys.ebi.gatling.recorder.configuration.Configuration.getConfigurationInstance;
import static com.excilys.ebi.gatling.recorder.http.event.RecorderEventBus.getEventBus;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.EMPTY;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.codehaus.plexus.util.Base64;
import org.codehaus.plexus.util.SelectorUtils;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.excilys.ebi.gatling.recorder.configuration.Configuration;
import com.excilys.ebi.gatling.recorder.configuration.Pattern;
import com.excilys.ebi.gatling.recorder.http.GatlingHttpProxy;
import com.excilys.ebi.gatling.recorder.http.event.BasicAuth;
import com.excilys.ebi.gatling.recorder.http.event.PauseEvent;
import com.excilys.ebi.gatling.recorder.http.event.RequestReceivedEvent;
import com.excilys.ebi.gatling.recorder.http.event.ResponseReceivedEvent;
import com.excilys.ebi.gatling.recorder.http.event.SecuredHostConnectionEvent;
import com.excilys.ebi.gatling.recorder.http.event.ShowConfigurationFrameEvent;
import com.excilys.ebi.gatling.recorder.http.event.ShowRunningFrameEvent;
import com.excilys.ebi.gatling.recorder.http.event.TagEvent;
import com.excilys.ebi.gatling.recorder.ui.enumeration.FilterStrategy;
import com.google.common.eventbus.Subscribe;

@SuppressWarnings("serial")
public class RunningFrame extends JFrame {

	private static final Logger LOGGER = LoggerFactory.getLogger(RunningFrame.class);
	private static final Format DATE_FORMAT = FastDateFormat.getInstance("yyyyMMddHHmmss");
	private static final int EVENTS_GROUPING = 100;

	private final JTextField txtTag = new JTextField(15);
	private final JButton btnTag = new JButton("Add");
	private final DefaultListModel events = new DefaultListModel();
	private final JList executedEvents = new JList(events);
	private final DefaultListModel hostsCertificate = new DefaultListModel();
	private final JList requiredHostsCertificate = new JList(hostsCertificate);
	private final TextAreaPanel stringRequest = new TextAreaPanel("Request:");
	private final TextAreaPanel stringResponse = new TextAreaPanel("Response:");
	private final TextAreaPanel stringRequestBody = new TextAreaPanel("Request Body:");
	private final TextAreaPanel stringResponseBody = new TextAreaPanel("Response Body:");

	private final RunningFrameState state = new RunningFrameState();

	public static class RunningFrameState {
		private Configuration configuration = null;
		private GatlingHttpProxy proxy = null;
		private Date startDate = new Date();
		private String startDateString = null;
		private PauseEvent pause = null;
		private BasicAuth basicAuth = null;
		private AtomicInteger requestCount = new AtomicInteger();

		private Date lastRequestDate = new Date();
		private URI baseURI = null;
		private Map<String, Map<String, String>> headers = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, String>>());
		private List<Object> scenarioEvents = Collections.synchronizedList(new ArrayList<Object>());

		public synchronized void initBaseURI(URI uri) {
			if (baseURI == null)
				baseURI = uri;
		}

		public synchronized void clear() {
			scenarioEvents.clear();
			headers.clear();
			baseURI = null;
			lastRequestDate = null;
		}

		public synchronized Configuration getConfiguration() {
			return configuration;
		}

		public synchronized void setConfiguration(Configuration configuration) {
			this.configuration = configuration;
		}

		public synchronized GatlingHttpProxy getProxy() {
			return proxy;
		}

		public synchronized void setProxy(GatlingHttpProxy proxy) {
			this.proxy = proxy;
		}

		public synchronized Date getStartDate() {
			return startDate;
		}

		public synchronized void setStartDate(Date startDate) {
			this.startDate = startDate;
			this.startDateString = DATE_FORMAT.format(startDate);
		}

		public synchronized String getStartDateString() {
			return startDateString;
		}

		public synchronized void setStartDateString(String startDateString) {
			this.startDateString = startDateString;
		}

		public synchronized Date getLastRequestDate() {
			return lastRequestDate;
		}

		public synchronized void setLastRequestDate(Date lastRequestDate) {
			this.lastRequestDate = lastRequestDate;
		}

		public synchronized PauseEvent getPause() {
			return pause;
		}

		public synchronized void setPause(PauseEvent pause) {
			this.pause = pause;
		}

		public synchronized BasicAuth getBasicAuth() {
			return basicAuth;
		}

		public synchronized void setBasicAuth(BasicAuth basicAuth) {
			this.basicAuth = basicAuth;
		}

		public synchronized AtomicInteger getRequestCount() {
			return requestCount;
		}

		public synchronized void setRequestCount(AtomicInteger requestCount) {
			this.requestCount = requestCount;
		}

		public synchronized URI getBaseURI() {
			return baseURI;
		}

		public synchronized void setBaseURI(URI baseURI) {
			this.baseURI = baseURI;
		}

		public synchronized Map<String, Map<String, String>> getHeaders() {
			return headers;
		}

		public synchronized void setHeaders(Map<String, Map<String, String>> headers) {
			this.headers = headers;
		}

		public synchronized List<Object> getScenarioEvents() {
			return scenarioEvents;
		}

		public synchronized void setScenarioEvents(List<Object> scenarioEvents) {
			this.scenarioEvents = scenarioEvents;
		}
	}

	public RunningFrame() {

		/* Initialization of the frame */
		setTitle("Gatling Recorder - Running...");
		setMinimumSize(new Dimension(1024, 815));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		setIconImages(Commons.getIconList());

		/* Declaration & initialization of components */
		JButton btnClear = new JButton("Clear");
		final JButton btnStop = new JButton("Stop !");
		btnStop.setSize(120, 30);

		JScrollPane panelFilters = new JScrollPane(executedEvents);
		panelFilters.setPreferredSize(new Dimension(300, 100));

		stringRequest.setPreferredSize(new Dimension(330, 100));
		JSplitPane requestResponsePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(stringRequest), new JScrollPane(stringResponse));
		JSplitPane bodiesPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stringRequestBody, stringResponseBody);
		final JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, requestResponsePane, bodiesPane);

		JScrollPane panelHostsCertificate = new JScrollPane(requiredHostsCertificate);
		panelHostsCertificate.setPreferredSize(new Dimension(300, 100));

		/* Layout */
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 5, 0, 0);

		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridy = 0;
		add(new JLabel("Tag :"), gbc);

		gbc.gridx = 1;
		add(txtTag, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0.5;
		add(btnTag, gbc);

		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.weightx = 0.25;
		add(btnClear, gbc);

		gbc.gridx = 4;
		gbc.anchor = GridBagConstraints.LINE_END;
		add(btnStop, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(new JLabel("Executed Events:"), gbc);

		gbc.gridy = 2;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.weighty = 0.20;
		gbc.fill = GridBagConstraints.BOTH;
		add(panelFilters, gbc);

		gbc.gridy = 4;
		gbc.weightx = 1;
		gbc.weighty = 0.75;
		gbc.fill = GridBagConstraints.BOTH;
		add(sp, gbc);

		gbc.gridy = 5;
		gbc.weighty = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.CENTER;
		add(new JLabel("Secured hosts requiring accepting a certificate:"), gbc);

		gbc.gridy = 6;
		gbc.weighty = 0.05;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(panelHostsCertificate, gbc);

		/* Listeners */
		btnTag.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (!txtTag.getText().isEmpty()) {
					TagEvent tag = new TagEvent(txtTag.getText());
					events.addElement(tag.toString());
					executedEvents.ensureIndexIsVisible(events.getSize() - 1);
					state.getScenarioEvents().add(tag);
					txtTag.setText(EMPTY);
				}
			}
		});

		executedEvents.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (executedEvents.getSelectedIndex() >= 0) {
					Object obj = state.getScenarioEvents().get(executedEvents.getSelectedIndex());
					if (obj instanceof ResponseReceivedEvent) {
						ResponseReceivedEvent event = (ResponseReceivedEvent) obj;
						stringRequest.txt.setText(event.getRequest().toString());
						stringResponse.txt.setText(event.getResponse().toString());
						stringRequestBody.txt.setText(event.getRequestContent());
						stringResponseBody.txt.setText(event.getResponseContent());

					} else {
						stringRequest.txt.setText(EMPTY);
						stringResponse.txt.setText(EMPTY);
						stringRequestBody.txt.setText(EMPTY);
						stringResponseBody.txt.setText(EMPTY);
					}
				}
			}
		});

		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearOldRunning();
			}
		});

		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveScenario();
				state.getProxy().shutdown();
				state.setProxy(null);
				getEventBus().post(new ShowConfigurationFrameEvent());
			}
		});
	}

	@Subscribe
	public void onShowConfigurationFrameEvent(ShowConfigurationFrameEvent event) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setVisible(false);
			}
		});
	}

	@Subscribe
	public void onShowRunningFrameEvent(ShowRunningFrameEvent event) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setVisible(true);
				clearOldRunning();
				Configuration configuration = getConfigurationInstance();
				state.setConfiguration(configuration);
				state.setStartDate(new Date());
				GatlingHttpProxy proxy = new GatlingHttpProxy(configuration.getPort(), configuration.getSslPort(), configuration.getProxy());
				state.setProxy(proxy);
				proxy.start();
			}
		});
	}

	@Subscribe
	public void onSecuredHostConnectionEvent(final SecuredHostConnectionEvent event) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (!hostsCertificate.contains(event.getHostURI()))
					hostsCertificate.addElement(event.getHostURI());
			}
		});
	}

	@Subscribe
	public void onRequestReceivedEvent(final RequestReceivedEvent event) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (event.getRequest().getMethod() == HttpMethod.CONNECT)
					return;

				String header = event.getRequest().getHeader("Proxy-Authorization");
				if (header != null) {
					// Split on " " and take 2nd group (Basic
					// credentialsInBase64==)
					String credentials = new String(Base64.decodeBase64(header.split(" ")[1].getBytes()));
					state.getConfiguration().getProxy().setUsername(credentials.split(":")[0]);
					state.getConfiguration().getProxy().setPassword(credentials.split(":")[1]);
				}

				if (addRequest(event.getRequest())) {
					Date lastRequestDate = state.getLastRequestDate();
					if (lastRequestDate != null) {
						Date newRequest = new Date();
						long diff = newRequest.getTime() - lastRequestDate.getTime();
						state.setLastRequestDate(newRequest);
						state.setPause(new PauseEvent(diff));
					}
				}
			}
		});
	}

	@Subscribe
	public void onResponseReceivedEvent(final ResponseReceivedEvent event) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (addRequest(event.getRequest())) {
					PauseEvent pause = state.getPause();
					if (pause != null) {
						events.addElement(pause.toString());
						executedEvents.ensureIndexIsVisible(events.getSize() - 1);
						state.getScenarioEvents().add(pause);
					}
					state.setLastRequestDate(new Date());
					processRequest(event);
				}
			}
		});
	}

	private void clearOldRunning() {
		events.removeAllElements();
		stringRequest.txt.setText(EMPTY);
		stringRequestBody.txt.setText(EMPTY);
		stringResponseBody.txt.setText(EMPTY);
		stringResponse.txt.setText(EMPTY);
		state.clear();
	}

	private boolean addRequest(HttpRequest request) {
		URI uri = null;
		try {
			uri = new URI(request.getUri());
		} catch (URISyntaxException ex) {
			LOGGER.error("Can't create URI from request uri ({}) {}", request.getUri(), ex.getStackTrace());
			// FIXME error handling
			return false;
		}

		Configuration configuration = state.getConfiguration();
		if (configuration.getFilterStrategy() != FilterStrategy.NONE) {

			String p = EMPTY;
			boolean add = true;
			if (configuration.getFilterStrategy() == FilterStrategy.ONLY)
				add = true;
			else if (configuration.getFilterStrategy() == FilterStrategy.EXCEPT)
				add = false;

			for (Pattern pattern : configuration.getPatterns()) {
				switch (pattern.getPatternType()) {
				case ANT:
					p = SelectorUtils.ANT_HANDLER_PREFIX;
					break;
				case JAVA:
					p = SelectorUtils.REGEX_HANDLER_PREFIX;
					break;
				}
				p += pattern.getPattern() + SelectorUtils.PATTERN_HANDLER_SUFFIX;
				if (SelectorUtils.matchPath(p, uri.getPath()))
					return add;
			}
			return !add;
		}
		return true;
	}

	private void processRequest(ResponseReceivedEvent event) {

		// set id
		int id = state.getRequestCount().getAndIncrement();
		event.setId(id);

		HttpRequest request = event.getRequest();

		URI uri = null;
		try {
			uri = new URI(request.getUri());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		events.addElement(request.getMethod() + " | " + request.getUri());
		executedEvents.ensureIndexIsVisible(events.getSize() - 1);

		/* URLs */
		state.initBaseURI(uri);

		event.computeUrls(state.getBaseURI());

		String headerAuthorization = event.getRequest().getHeader("Authorization");
		request.removeHeader("Authorization");
		if (headerAuthorization != null) {
			if (state.getBasicAuth() == null) {
				// Split on " " and take 2nd group (Basic credentialsInBase64==)
				String credentials = new String(Base64.decodeBase64(headerAuthorization.split(" ")[1].getBytes()));
				state.setBasicAuth(new BasicAuth(event.getRequestAbsoluteUrl(), credentials.split(":")[0], credentials.split(":")[1]));
				event.setBasicAuth(state.getBasicAuth());
			} else {
				if (event.getRequestAbsoluteUrl().equals(state.getBasicAuth().getUrlBase()))
					event.setBasicAuth(state.getBasicAuth());
				else
					state.setBasicAuth(null);
			}
		}

		/* Headers */
		Map<String, String> requestHeaders = new TreeMap<String, String>();

		for (Entry<String, String> entry : request.getHeaders()) {
			String headerName = entry.getKey();
			if (!headerName.equals("Cookie") || !headerName.equals("Content-Length"))
				requestHeaders.put(entry.getKey(), entry.getValue());
		}

		String headersId = null;
		for (Entry<String, Map<String, String>> headersEntry : state.getHeaders().entrySet()) {

			Map<String, String> headersGroup = headersEntry.getValue();

			if (headersGroup.equals(requestHeaders)) {
				headersId = headersEntry.getKey();
				break;
			}
		}

		if (headersId == null) {
			headersId = "headers_" + state.getHeaders().size();
			state.getHeaders().put(headersId, requestHeaders);

		}

		event.setHeadersId(headersId);

		/* Add check if status is not in 20X */
		if (isStatusCodeNon2XX(event.getResponse().getStatus()))
			event.setWithCheck(true);

		/* Params */
		QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
		event.getRequestParams().putAll((decoder.getParameters()));

		/* Content */
		if (request.getContent().capacity() > 0) {
			String content = new String(request.getContent().array());
			// We check if it's a form validation and so we extract post params
			if ("application/x-www-form-urlencoded".equals(request.getHeader("Content-Type"))) {
				decoder = new QueryStringDecoder("http://localhost/?" + content);
				event.getRequestParams().putAll(decoder.getParameters());
			} else {
				event.setWithBody(true);
				dumpRequestBody(id, content);
			}
		}

		state.getScenarioEvents().add(event);
	}

	private boolean isStatusCodeNon2XX(HttpResponseStatus status) {
		return status.getCode() < 200 || status.getCode() > 210;
	}

	private void dumpRequestBody(int idEvent, String content) {

		File dir = getRequestBodiesOutputFolder();
		FileWriter fw = null;
		try {
			fw = new FileWriter(new File(dir, state.getStartDateString() + "_request_" + idEvent + ".txt"));
			fw.write(content);
		} catch (IOException e) {
			LOGGER.error("Error, while dumping request body... {}", e);
		} finally {
			closeQuietly(fw);
		}
	}

	private File getRequestBodiesOutputFolder() {
		File dir = new File(state.getConfiguration().getRequestBodiesFolder());
		dir.mkdir();
		return dir;
	}

	private File getSimulationOutputFolder() {
		Configuration configuration = state.getConfiguration();
		StringBuilder path = new StringBuilder().append(configuration.getOutputFolder());
		if (configuration.getSimulationPackage() != null)
			path.append(File.separator).append(configuration.getSimulationPackage().replace(".", File.separator)).toString();
		File dir = new File(path.toString());
		dir.mkdirs();
		return dir;
	}

	private String getSimulationFileName() {
		return getConfigurationInstance().getSimulationClassName() + state.getStartDateString();
	}

	private boolean isRedirect(HttpResponse response) {
		int responseStatus = response.getStatus().getCode();
		return responseStatus == 301 || responseStatus == 302;
	}

	private List<Object> filterEvents() {

		if (state.getConfiguration().isFollowRedirect()) {

			List<Object> filteredEvents = new ArrayList<Object>();

			ResponseReceivedEvent redirectChainStart = null;

			for (Object event : state.getScenarioEvents()) {
				if (event instanceof ResponseReceivedEvent) {
					ResponseReceivedEvent responseReceivedEvent = ResponseReceivedEvent.class.cast(event);

					if (isRedirect(responseReceivedEvent.getResponse())) {

						if (redirectChainStart == null)
							// reaching start of redirect chain
							redirectChainStart = responseReceivedEvent;

					} else if (redirectChainStart != null) {
						// reaching end of redirect chain
						// create a new wrapper event
						ResponseReceivedEvent wrapper = new ResponseReceivedEvent(redirectChainStart.getRequest(), responseReceivedEvent.getResponse(), null, null);
						wrapper.setId(redirectChainStart.getId());
						wrapper.setHeadersId(redirectChainStart.getHeadersId());
						wrapper.setBasicAuth(redirectChainStart.getBasicAuth());
						wrapper.setRequestParams(redirectChainStart.getRequestParams());
						wrapper.setWithBody(redirectChainStart.isWithBody());
						wrapper.setWithCheck(isStatusCodeNon2XX(responseReceivedEvent.getResponse().getStatus()));
						wrapper.computeUrls(state.getBaseURI());
						filteredEvents.add(wrapper);

						// reset
						redirectChainStart = null;

					} else {
						// not inside a redirect chain
						filteredEvents.add(event);
					}
				} else if (redirectChainStart == null) {
					// not inside a redirect chain
					filteredEvents.add(event);
				}
				// else, dropping it
			}
			return filteredEvents;

		} else {
			return state.getScenarioEvents();
		}
	}

	private Map<String, Map<String, String>> filterHeaders(List<Object> events) {

		Map<String, Map<String, String>> filteredHeaders = new TreeMap<String, Map<String, String>>();

		for (Object scenarioEvent : events) {
			if (scenarioEvent instanceof ResponseReceivedEvent) {
				String headersId = ResponseReceivedEvent.class.cast(scenarioEvent).getHeadersId();
				filteredHeaders.put(headersId, state.getHeaders().get(headersId));
			}
		}

		return filteredHeaders;
	}

	private void saveScenario() {

		List<Object> filteredEvents = filterEvents();
		Map<String, Map<String, String>> filteredHeaders = filterHeaders(filteredEvents);
		File simulationOutputFolder = getSimulationOutputFolder();
		String simulationClassName = getSimulationFileName();

		VelocityEngine ve = new VelocityEngine();
		ve.setProperty("file.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.init();

		VelocityContext context = new VelocityContext();
		context.put("baseURI", state.getBaseURI());
		context.put("proxy", state.getConfiguration().getProxy());
		context.put("followRedirect", state.getConfiguration().isFollowRedirect());
		context.put("headers", filteredHeaders);
		context.put("name", "Scenario name");

		if (filteredEvents.size() > EVENTS_GROUPING) {
			List<List<Object>> subListsEvents = new ArrayList<List<Object>>();
			int numberOfSubLists = filteredEvents.size() / EVENTS_GROUPING + 1;
			for (int i = 0; i < numberOfSubLists; i++)
				subListsEvents.add(filteredEvents.subList(0 + EVENTS_GROUPING * i, Math.min(EVENTS_GROUPING * (i + 1), filteredEvents.size() - 1)));

			context.put("chainEvents", subListsEvents);
			context.put("events", new ArrayList<Object>());
		} else {
			context.put("events", filteredEvents);
			context.put("chainEvents", new ArrayList<List<Object>>());
		}

		context.put("package", getConfigurationInstance().getSimulationPackage());
		context.put("className", simulationClassName);
		context.put("date", state.getStartDateString());

		Template template = null;
		Writer writer = null;
		try {
			template = ve.getTemplate("simulation.vm");
			writer = new OutputStreamWriter(new FileOutputStream(new File(simulationOutputFolder, simulationClassName + ".scala")), state.getConfiguration().getEncoding());
			template.merge(context, writer);
			writer.flush();

		} catch (IOException e) {
			LOGGER.error("Error, while saving scenario..." + e.getStackTrace());

		} finally {
			closeQuietly(writer);
		}
	}
}
