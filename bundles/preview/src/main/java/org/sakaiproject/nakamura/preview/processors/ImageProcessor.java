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
package org.sakaiproject.nakamura.preview.processors;


import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.util.IOUtils;
import org.sakaiproject.nakamura.preview.ProcessingException;
import org.sakaiproject.nakamura.preview.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service(ImageProcessor.class)
@Component(immediate = true)
public class ImageProcessor {

  static final Logger log = LoggerFactory.getLogger(ImageProcessor.class);

  /**
   * resize and save the image
   * @param inputPath the path to the input file
   * @param suffix an identifier for the thumbnail
   * @param maxWidth the maximum width of the resized image (in pixels)
   * @param maxHeight the maximum height of the resized image (in pixels)
   * @throws ProcessingException
   */
  public void resize(String inputPath, String outputPath, Double maxWidth, Double maxHeight)
  throws ProcessingException {
    File input = new File(inputPath);
    File dst = new File(outputPath);

    try {
      ImageInfo info = Sanselan.getImageInfo(input);
      BufferedImage image = ImageUtils.getBufferedImage(IOUtils.getFileBytes(input), info);
      Dimension d = Sanselan.getImageSize(input);

      Double width = d.getWidth();
      Double height = d.getHeight();
      Double ratio = width / maxWidth;

      if (maxHeight == null){
        maxHeight = height / ratio;
      }

      Double imageRatio = width / height;
      Double scaleRatio = 0.0;
      if (imageRatio > ratio){
        scaleRatio = maxWidth / width;
      }
      else {
        scaleRatio = maxHeight / height;
      }
      int targetWidth = (int)(width * scaleRatio);
      int targetHeight = (int)(height * scaleRatio);
      ImageUtils.write(ImageUtils.getScaledInstance(image, targetWidth, targetHeight), info, dst);

      log.debug("Wrote image {}h x {}w to {}",
          new Object[] { Integer.toString(targetHeight), Integer.toString(targetWidth), dst.getAbsolutePath()});
    } catch (ImageReadException e) {
      log.error("Error reading image at {}, {}", inputPath, e);
      throw new ProcessingException("Error reading image " + inputPath, e);
    }  catch (IOException e) {
      log.error("Error reading image at {}, {}", inputPath, e);
      throw new ProcessingException("Error reading image " + inputPath, e);
    } catch (ImageWriteException e) {
      log.error("Error writing image at {}, {}", inputPath, e);
      throw new ProcessingException("Error writing image " + inputPath, e);
    }
  }
}
