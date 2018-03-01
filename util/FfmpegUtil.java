package com.onemt.news.crawler.dynamicpic.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FfmpegUtil {

	private static final Logger logger = LoggerFactory.getLogger(FfmpegUtil.class);
	private static final String os = System.getProperty("os.name");
	private static final ClassLoader classLoader = FfmpegUtil.class.getClassLoader();
	private static final String convertStr = "%s/ffmpeg -i %s -y -r 10 -loop 0 %s";
	private static final String firstFrameToWebp = "%s/ffmpeg -i %s -y -vframes 1 %s";
	private static final String first50FramesGif = "%s/ffmpeg -i %s -y -vframes %s -loop 0 %s";
	//获取文件内容信息:ffprobe -v quiet -print_format json -show_format -show_streams -count_frames -show_packets D:\data\crawler\article\webp\2b24b6f403f346529bd38b4c87dcbf4b.webp
	private static String ffmpegPath;

	static {
		if (os.toLowerCase().startsWith("win")) {
			// ffmpeg
			ffmpegPath = classLoader.getResource("ffmpeg/windows-x64").getPath();
			logger.info("ffmpegPath:" + ffmpegPath);
		} else {
			ffmpegPath = classLoader.getResource("ffmpeg/linux-x64").getPath();
			logger.info("ffmpegPath:" + ffmpegPath);
			try {
				Runtime.getRuntime().exec("chmod -R 755 " + ffmpegPath).waitFor();
			} catch (InterruptedException | IOException e) {
				logger.error(" ffmpeg chmod error >>> {}", e.getMessage());
			}
		}
	}

	/**
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @param quality
	 */
	public static void convert(String inputFile, String outputFile) throws Exception {
		Process exec = null;
		try {
			String execStrFormat = String.format(convertStr, ffmpegPath, inputFile, outputFile);
			logger.debug("execStrFormat:" + execStrFormat);
			exec = Runtime.getRuntime().exec(execStrFormat);
			exec.waitFor();
		} catch (Exception e) {
			logger.error("An error happend when convert {} to {} .", inputFile, outputFile, e);
			throw e;
		} finally {
			if (exec != null) {
				exec.destroy();
			}
		}
	}

	/**
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @param quality
	 */
	public static void convertFirstFrameToWebp(String inputFile, String outputFile) throws Exception {
		Process exec = null;
		try {
			String execStrFormat = String.format(firstFrameToWebp, ffmpegPath, inputFile, outputFile);
			logger.debug("execStrFormat first frame to webp :" + execStrFormat);
			exec = Runtime.getRuntime().exec(execStrFormat);
			exec.waitFor();
		} catch (Exception e) {
			logger.error("An error happend when first frame to webp . file is: {}", inputFile, e);
			throw e;
		} finally {
			if (exec != null) {
				exec.destroy();
			}
		}
	}

	public static void getFirst50FramesGif(String inputFilePath, String outputFilePath) throws Exception {
		getSpecifiedFramesCountGif(inputFilePath, outputFilePath, 50);
	}

	/**
	 * 截取指定张数的gif图片
	 *
	 * @param inputFilePath
	 * @param outputFilePath
	 * @throws Exception
	 */
	public static void getSpecifiedFramesCountGif(String inputFilePath, String outputFilePath, int count) throws Exception {
		Process exec = null;
		try {
			String execStrFormat = String.format(first50FramesGif, ffmpegPath, inputFilePath, count, outputFilePath);
			logger.debug("execStrFormat first frame to webp :" + execStrFormat);
			exec = Runtime.getRuntime().exec(execStrFormat);
			exec.waitFor();
		} catch (Exception e) {
			logger.error("An error happend when first frame to webp . file is: {}", inputFilePath, e);
			throw e;
		} finally {
			if (exec != null) {
				exec.destroy();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		String inputFilePath = "E:\\testFrams\\tenor(1).gif";
		String outputFilePath = "E:\\testFrams\\tenor(1)first5.gif";
		getSpecifiedFramesCountGif(inputFilePath,outputFilePath,5);
//		Files.list(Paths.get("D:\\data\\crawler\\article\\gif")).forEach(file -> {
//			try {
//				if(!file.toString().endsWith(".mp4"))
//					return;
//				System.out.println(file.toString());
//				long start = System.currentTimeMillis();
//				convert(file.toString(), file.toString().replaceFirst("mp4$", "gif"));
//				convert(file.toString(), file.toString().replaceFirst("mp4$", "webp"));
//				convertFirstFrameToWebp(file.toString(), file.toString().replaceFirst("mp4$", "_convert.webp"));
//				long end = System.currentTimeMillis();
//				System.out.println(String.format("convert used time :%d ms", (end - start)));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		});

	}

}
