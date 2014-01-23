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
package io.gatling.recorder.scenario.template

import io.gatling.recorder.enumeration.FilterStrategy._
import io.gatling.core.util.StringHelper.eol
import io.gatling.recorder.config.RecorderConfiguration
import io.gatling.recorder.scenario.ProtocolDefinition.baseHeaders
import com.dongxiguo.fastring.Fastring.Implicits._
import io.gatling.recorder.scenario.ProtocolDefinition

object ProtocolTemplate {

	val indent = "\t" * 2

	def render(protocol: ProtocolDefinition)(implicit config: RecorderConfiguration) = {

		def renderProxy = {

			def renderSslPort = config.proxy.outgoing.sslPort.map(proxySslPort => s".httpsPort($proxySslPort)").getOrElse("")

			def renderCredentials = {
				val credentials = for {
					proxyUsername <- config.proxy.outgoing.username
					proxyPassword <- config.proxy.outgoing.password
				} yield s"""$eol$indent.credentials("$proxyUsername","$proxyPassword")"""
				credentials.getOrElse("")
			}

			val protocol = for {
				proxyHost <- config.proxy.outgoing.host
				proxyPort <- config.proxy.outgoing.port
			} yield fast"""$eol$indent.proxy(Proxy("$proxyHost", $proxyPort)$renderSslPort$renderCredentials)"""

			protocol.getOrElse(fast"")
		}

		def renderFollowRedirect = if (!config.http.followRedirect) fast"$eol$indent.disableFollowRedirect" else fast""

		def renderFetchHtmlResources = if (config.http.fetchHtmlResources) {
			val filtersConfig = config.filters

			def quotedStringList(xs: Seq[String]): String = xs.map(p => "\"\"\"" + p + "\"\"\"").mkString(", ")
			def backlistPatterns = fast"black = BlackList(${quotedStringList(filtersConfig.blackList.patterns)})"
			def whitelistPatterns = fast"white = WhiteList(${quotedStringList(filtersConfig.whiteList.patterns)})"

			val patterns = filtersConfig.filterStrategy match {
				case WHITELIST_FIRST => fast"$whitelistPatterns, $backlistPatterns"
				case BLACKLIST_FIRST => fast"$backlistPatterns, $whitelistPatterns"
				case DISABLED => fast"white = WhiteList()"
			}

			fast"$eol$indent.fetchHtmlResources($patterns)"
		} else fast""

		def renderAutomaticReferer = if (!config.http.automaticReferer) fast"$eol$indent.disableAutoReferer" else fast""

		def renderHeaders = {
			def renderHeader(methodName: String, headerValue: String) = fast"""$eol$indent.$methodName(\"\"\"$headerValue\"\"\")"""
			protocol.headers.toList.sorted.flatMap { case (headerName, headerValue) => baseHeaders.get(headerName).map(renderHeader(_, headerValue)) }.mkFastring
		}

		fast"""
		.baseURL("${protocol.baseUrl}")$renderProxy$renderFollowRedirect$renderFetchHtmlResources$renderAutomaticReferer$renderHeaders""".toString
	}
}