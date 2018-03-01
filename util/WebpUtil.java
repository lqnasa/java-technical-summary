package com.onemt.news.crawler.dynamicpic.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: WebpUtil
 * @Description: webp格式工具类
 *               https://developers.google.com/speed/webp/docs/gif2webp
 * @author liqiao
 * @date 2017-11-3 上午11:50:43
 * @version: V1.0
 */
public class WebpUtil {

	private static final Logger logger = LoggerFactory.getLogger(WebpUtil.class);

	private static final String OS_NAME = System.getProperty("os.name");
	private static final ClassLoader CLASS_LOADER = WebpUtil.class.getClassLoader();
	private static final String CWEBP_COMMAND_PATTERN = "%scwebp -q %d %s -o %s";
	private static final String GIF2WEBP_COMMAND_PATTERN = "%sgif2webp -lossy -m 6 -mt -q %d %s -o %s";
	private static final int CONVERT_QUALITY = 50;
	private static String LIB_WEBP_PATH;

	static {
		if (OS_NAME.toLowerCase().startsWith("win")) {
			// libwebp
			LIB_WEBP_PATH = CLASS_LOADER.getResource("libwebp/windows-x64/").getPath();
			logger.info("libwebpPath" + LIB_WEBP_PATH);

		} else {
			LIB_WEBP_PATH = CLASS_LOADER.getResource("libwebp/linux-x64/").getPath();
			logger.info("libwebpPath" + LIB_WEBP_PATH);
			try {
				Runtime.getRuntime().exec("chmod -R 755 " + LIB_WEBP_PATH).waitFor();
			} catch (InterruptedException | IOException e) {
				logger.error(" libwebp chmod error >>> {}", e.getMessage());
			}
		}
	}

	/**
	 * @param inputFile
	 * @param outputFile
	 * @param imgType
	 * @throws Exception
	 */
	public static void convertToWebp(String inputFile, String outputFile, String imgType) throws Exception {
		convertToWebp(inputFile, outputFile, CONVERT_QUALITY, imgType);
	}

	/**
	 * @param inputFile
	 * @param outputFile
	 * @param quality
	 * @param imgType
	 * @throws Exception
	 */
	public static void convertToWebp(String inputFile, String outputFile, Integer quality, String imgType)
			throws Exception {
		executeCWebp(inputFile, outputFile, quality, imgType);
	}

	/**
	 * cwebp [options] input_file -o output_file.webp gif2webp [options]
	 * input_file.gif -o output_file.webp
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @param quality
	 */
	private static void executeCWebp(String inputFile, String outputFile, Integer quality, String imgType)
			throws Exception {
		Process exec = null;
		try {
			String formatPattern = "gif".equals(imgType) ? GIF2WEBP_COMMAND_PATTERN : CWEBP_COMMAND_PATTERN;
			String execStrFormat = String.format(formatPattern, LIB_WEBP_PATH, quality, inputFile, outputFile);
			logger.debug("execStrFormat:" + execStrFormat);
			exec = Runtime.getRuntime().exec(execStrFormat);
			exec.waitFor();

		} catch (Exception e) {
			logger.error("An error happend when convert to webp. Img is: " + inputFile, e);
			throw e;

		} finally {
			if (exec != null) {
				exec.destroy();
			}
		}
	}

}
