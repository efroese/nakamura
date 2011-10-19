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
package org.sakaiproject.nakamura.maven;


import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class DroolsCompilerMojoTest {

  @Test
  public void testExecute() throws MojoExecutionException, IOException {
    DroolsCompilerMojo d = new DroolsCompilerMojo();
    d.excludes = new String[] { "**/Bad*"};
    d.includes = new String[] { "**/*" };
    d.rulesdir = new File("src/test/rules");
    d.outputDirectory = new File("target/testoutput1/classes");
    d.packageOutputName = "SLING-IN/test.pkg";
    
    FileUtils.deleteDirectory(d.outputDirectory);
    
    d.rulesdir.mkdirs();
    d.outputDirectory.mkdirs();
    
    File testClasses = new File("target/test-classes");
    if ( testClasses.exists() ) {
      FileUtils.copyDirectoryStructure(testClasses, d.outputDirectory);
    }
    FileUtils.copyDirectoryStructure(new File("target/classes/"), d.outputDirectory);
    
    Model model = new Model();
    d.project = new MavenProject(model);
    
    d.execute();
    
    
    
    
  }
  

  @Test
  public void testExecuteMissing() throws MojoExecutionException, IOException {
    DroolsCompilerMojo d = new DroolsCompilerMojo();
    d.excludes = new String[] {};
    d.includes = new String[] { "**/*" };
    d.rulesdir = new File("src/test/rules");
    d.outputDirectory = new File("target/testoutput2/classes");
    d.packageOutputName = "SLING-IN/test.pkg";
    FileUtils.deleteDirectory(d.outputDirectory);
    
    d.rulesdir.mkdirs();
    d.outputDirectory.mkdirs();
    Model model = new Model();
    d.project = new MavenProject(model);
    
    try {
    
      d.execute();
      FileUtils.deleteDirectory(d.outputDirectory);
      Assert.fail();
    } catch ( MojoExecutionException e ) {
      Assert.assertEquals("Package is not valid", e.getMessage());
    }
    FileUtils.deleteDirectory(d.outputDirectory);
    
    
    
  }

}
