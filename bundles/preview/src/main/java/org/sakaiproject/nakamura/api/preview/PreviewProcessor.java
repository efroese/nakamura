/**
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.api.preview;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public interface PreviewProcessor {

	public static final String MIME_TYPE      = "_mimeType";
	public static final String PROCESSED_BY   = "sakai:processed_by";
	public static final String FILE_EXTENSION = "sakai:fileextension";

	public static final Set<String> IMAGE_EXTENSIONS = 
		ImmutableSet.of("jpg", "jpeg", "png", "gif", "psd");

	public static final Set<String> PDF_EXTENSIONS = 
		ImmutableSet.of("pdf");

	public static final Set<String> FIRST_PAGE_ONLY_EXTENSIONS = 
		ImmutableSet.of("htm", "html", "xhtml", "txt");
}
