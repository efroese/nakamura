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
package org.sakaiproject.nakamura.preview.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

public class FilePathUtils {
	/**
	 * List the files in a directory and sort them by name.
	 * @param dirPath
	 * @return
	 */
	public static File[] listFilesSortedName(String dirPath){
		File[] files = new File(dirPath).listFiles();
		Arrays.sort(files,
			new Comparator<File>(){
				public int compare(final File f1, final File f2) {
					return f1.getName().compareTo(f2.getName());
				}
			});
		return files;
	}

	/**
	 * List sub-directories and sort them by name 
	 * @param dirPath
	 * @return
	 */
	public static File[] listDirsSortedName(String dirPath){
		// Only list directories
		File[] files = new File(dirPath).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		Arrays.sort(files,
			new Comparator<File>(){
				public int compare(final File f1, final File f2) {
					return f1.getName().compareTo(f2.getName());
				}
			});
		return files;
	}

	public static String join(String[] pieces){
	  return StringUtils.join(pieces, File.separator);
	}
}
