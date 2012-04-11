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
package org.sakaiproject.nakamura.preview;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sanselan/ImageIO shortcuts and workarounds
 */
public class ImageUtils {

	private static Logger log = LoggerFactory.getLogger(ImageUtils.class);

	/**
	 * @param bytes
	 * @param info
	 * @return
	 * @throws IOException
	 * @throws ImageReadException
	 * @throws ImageException
	 */
	public static BufferedImage getBufferedImage(byte[] bytes, ImageInfo info)
	throws ImageReadException, IOException {
		BufferedImage imgBuf;
		// Guess the format and check if it is a valid one.
		if (info.getFormat() == ImageFormat.IMAGE_FORMAT_UNKNOWN) {
			// This is not a valid image.
			log.error("Can't parse this format.");
			throw new ImageReadException("Can't parse this format: " + info.getFormatName());
		} else if (info.getFormat() == ImageFormat.IMAGE_FORMAT_JPEG) {
			imgBuf = ImageIO.read(new ByteArrayInputStream(bytes));
		} else {
			imgBuf = Sanselan.getBufferedImage(bytes);
			// KERN-1113 Sanselan doesn't read the image type correctly when working with some
			// PNG's.  Alpha layer is tricky.
			if (imgBuf.getType() == 0) {
				imgBuf = ImageIO.read(new ByteArrayInputStream(bytes));
			}
		}
		return imgBuf;
	}

	/**
	 * 
	 * @param img
	 * @param targetWidth
	 * @param targetHeight
	 * @return
	 */
	public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth,
			int targetHeight) {
		BufferedImage ret = img;

		// Use multi-step technique: start with original size, then
		// scale down in multiple passes with drawImage()
		// until the target size is reached
		int w = img.getWidth();
		int h = img.getHeight();

		while (w > targetWidth || h > targetHeight) {
			// Bit shifting by one is faster than dividing by 2.
			w >>= 1;
			if (w < targetWidth) {
				w = targetWidth;
			}

			// Bit shifting by one is faster than dividing by 2.
			h >>= 1;
			if (h < targetHeight) {
				h = targetHeight;
			}

			BufferedImage tmp = new BufferedImage(w, h, img.getType());
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();
			ret = tmp;
		}

		return ret;
	}

	/**
	 * Write out the image to a file.
	 * @param image the image to save
	 * @param info the {@link ImageInfo} for the image
	 * @param file where to write the file
	 * @throws IOException
	 * @throws ImageWriteException
	 */
	public static void write(BufferedImage image, ImageInfo info, File file)
	throws IOException, ImageWriteException {
		if (info.getFormat() == ImageFormat.IMAGE_FORMAT_UNKNOWN) {
			throw new ImageWriteException("Format not supported: " + info.getFormatName());
		} else if (info.getFormat() == ImageFormat.IMAGE_FORMAT_JPEG) {
			boolean success = ImageIO.write(image, ImageFormat.IMAGE_FORMAT_JPEG.name, file);
			if (!success){
				log.error("ImageIO failed to write {}", file.getAbsolutePath());
			}
		} else {
			Sanselan.writeImage(image, file, info.getFormat(), null);
		}
	}
}
