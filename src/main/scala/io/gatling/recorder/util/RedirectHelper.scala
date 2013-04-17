/**
 * Copyright 2011-2013 eBusiness Information, Groupe Excilys (www.excilys.com)
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
package io.gatling.recorder.util

import io.gatling.recorder.config.RecorderConfiguration.configuration

object RedirectHelper {

	def isRedirectCode(code: Int) = code >= 300 && code <= 399

	def isRequestRedirectChainStart(lastStatus: Int, currentStatus: Int): Boolean = configuration.http.followRedirect && !isRedirectCode(lastStatus) && isRedirectCode(currentStatus)

	def isRequestInsideRedirectChain(lastStatus: Int, currentStatus: Int): Boolean = configuration.http.followRedirect && isRedirectCode(lastStatus) && isRedirectCode(currentStatus)

	def isRequestRedirectChainEnd(lastStatus: Int, currentStatus: Int): Boolean = configuration.http.followRedirect && isRedirectCode(lastStatus) && !isRedirectCode(currentStatus)
}
