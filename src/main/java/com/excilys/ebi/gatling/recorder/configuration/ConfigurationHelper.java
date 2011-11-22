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

import static com.excilys.ebi.gatling.recorder.ui.Constants.GATLING_RECORDER_FILE_NAME;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.excilys.ebi.gatling.recorder.ui.enumeration.ResultType;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public final class ConfigurationHelper {

	private static final XStream XSTREAM = new XStream(new DomDriver());

	private static final File CONFIGURATION_FILE = new File(System.getProperty("user.home"), GATLING_RECORDER_FILE_NAME);

	static {
		XSTREAM.alias("resultType", ResultType.class);
		XSTREAM.alias("configuration", Configuration.class);
		XSTREAM.alias("pattern", Pattern.class);
	}

	private ConfigurationHelper() {
		throw new UnsupportedOperationException();
	}

	public static Configuration readFromDisk() {

		if (CONFIGURATION_FILE.exists()) {
			try {
				return (Configuration) XSTREAM.fromXML(CONFIGURATION_FILE);
			} catch (Exception e) {
				System.err.println(e);
				return null;
			}
		} else {
			return null;
		}
	}

	public static void saveToDisk(Configuration configuration) {

		FileWriter fw = null;
		try {
			fw = new FileWriter(CONFIGURATION_FILE);
			XSTREAM.toXML(configuration, fw);

		} catch (IOException e) {
			System.err.println(e);

		} finally {
			closeQuietly(fw);
		}
	}
}
