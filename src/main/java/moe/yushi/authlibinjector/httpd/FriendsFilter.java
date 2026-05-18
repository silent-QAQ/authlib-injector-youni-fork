/*
 * Copyright (C) 2023  Haowei Wen <yushijinhun@gmail.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package moe.yushi.authlibinjector.httpd;

import static java.util.Optional.empty;
import static moe.yushi.authlibinjector.util.Logging.log;
import static moe.yushi.authlibinjector.util.Logging.Level.DEBUG;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import moe.yushi.authlibinjector.internal.fi.iki.elonen.IHTTPSession;
import moe.yushi.authlibinjector.internal.fi.iki.elonen.Response;
import moe.yushi.authlibinjector.yggdrasil.YggdrasilClient;

public class FriendsFilter implements URLFilter {

	private YggdrasilClient customClient;

	private static final Set<String> FRIEND_DOMAINS = new HashSet<>();
	static {
		FRIEND_DOMAINS.add("userpresence.xboxlive.com");
		FRIEND_DOMAINS.add("profile.xboxlive.com");
		FRIEND_DOMAINS.add("social.xboxlive.com");
		FRIEND_DOMAINS.add("peoplehub.xboxlive.com");
	}

	public FriendsFilter(YggdrasilClient customClient) {
		this.customClient = customClient;
	}

	@Override
	public boolean canHandle(String domain) {
		return FRIEND_DOMAINS.contains(domain) || domain.endsWith(".xboxlive.com");
	}

	@Override
	public Optional<Response> handle(String domain, String path, IHTTPSession session) throws IOException {
		log(DEBUG, "Intercepting friends request to " + domain + path);
		
		// Check if custom client supports friends API
		if (customClient.supportsFriendsApi()) {
			// Let URLRedirector handle it, which will forward to custom server
			return empty();
		}
		
		// If no custom friends API, return empty to let reverse proxy handle it
		return empty();
	}
}
