/*
 * Copyright 2011 gitblit.com.
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
package com.gitblit.git;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.transport.resolver.UploadPackFactory;

import com.gitblit.GitBlit;
import com.gitblit.models.UserModel;

/**
 * The upload pack factory creates an upload pack which controls what refs are
 * advertised to cloning/pulling clients.
 *
 * @author James Moger
 *
 * @param <X> the connection type
 */
public class GitblitUploadPackFactory<X> implements UploadPackFactory<X> {

	@Override
	public UploadPack create(X req, Repository db)
			throws ServiceNotEnabledException, ServiceNotAuthorizedException {

		UserModel user = UserModel.ANONYMOUS;
		int timeout = 0;

		if (req instanceof HttpServletRequest) {
			// http/https request may or may not be authenticated
			user = GitBlit.self().authenticate((HttpServletRequest) req);
			if (user == null) {
				user = UserModel.ANONYMOUS;
			}
		} else if (req instanceof GitDaemonClient) {
			// git daemon request is always anonymous
			GitDaemonClient client = (GitDaemonClient) req;
			// set timeout from Git daemon
			timeout = client.getDaemon().getTimeout();
		}

		UploadPack up = new UploadPack(db);
		up.setTimeout(timeout);

		return up;
	}
}