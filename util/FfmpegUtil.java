package com.onemt.news.crawler.dynamicpic.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FfmpegUtil {

	private static final Logger logger = LoggerFactory.getLogger(FfmpegUtil.class);
	private static final String os = System.getProperty("os.name");
	private static final ClassLoader classLoader = FfmpegUtil.class.getClassLoader();
	private static final String convertStr = "%s/ffmpeg -i %s -y -r %s -f %s  -loop 0 %s";
	private static final String firstFrameToWebp = "%s/ffmpeg -i %s -y -vframes 1 %s";
	private static final String first50FramesGif = "%s/ffmpeg -i %s -y -vframes %s -loop 0 %s";
	private static final String ffprobeInfo = "%s/ffprobe -v quiet -print_format json -show_format -show_streams -count_frames -show_packets %s";

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
	 * 该方法支持视频转 动图 (gif,webp) 支持降低帧率 默认帧率10帧
	 * 
	 * @param inputFilePath 输入文件地址
	 * @param outputFilePath 输出文件地址
	 * @param formatType 转换类型(gif,webp,mp4) ffmpeg –formats 查看ffmpeg支持的转换类型
	 * @throws Exception
	 */
	public static void convert(String inputFilePath, String outputFilePath, String formatType) throws Exception {
		convert(inputFilePath, outputFilePath, 10, formatType);
	}

	/**
	 * 
	 * 该方法支持视频转 动图 (gif,webp) 支持降低帧率
	 * 
	 * @param inputFilePath 输入文件地址
	 * @param outputFilePath 输出文件地址
	 * @param frameRate  帧率
	 * @param formatType 转换类型(gif,webp,mp4) ffmpeg –formats 查看ffmpeg支持的转换类型
	 * @throws Exception
	 */
	public static void convert(String inputFilePath, String outputFilePath, int frameRate, String formatType)
			throws Exception {
		Process exec = null;
		try {
			String execStrFormat = String.format(convertStr, ffmpegPath, inputFilePath, frameRate, formatType,
					outputFilePath);
			logger.debug("execStrFormat convert :" + execStrFormat);
			exec = Runtime.getRuntime().exec(execStrFormat);
			exec.waitFor();
		} catch (Exception e) {
			logger.error("An error happend when convert Frame Rate. file is: {}", inputFilePath, e);
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
	public static void getSpecifiedFramesCountGif(String inputFilePath, String outputFilePath, int count)
			throws Exception {
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

	/**
	 * 获取文件内容信息:ffprobe -v quiet -print_format json -show_format -show_streams
	 * -count_frames -show_packets {inputFilePath}
	 * 
	 * @param inputFilePath 输入文件地址
	 * @return 返回json的文件内容信息
	 * @throws Exception
	 */
	public static String ffprobeInfo(String inputFilePath) throws Exception {
		Process exec = null;
		try {
			String execStrFormat = String.format(ffprobeInfo, ffmpegPath, inputFilePath);
			logger.debug("execStrFormat first frame to webp :" + execStrFormat);
			exec = Runtime.getRuntime().exec(execStrFormat);
			String result = new BufferedReader(new InputStreamReader(exec.getInputStream())).lines()
					.collect(Collectors.joining(""));
			return result;
		} catch (Exception e) {
			logger.error("An error happend when first frame to webp . file is: {}", inputFilePath, e);
			throw e;
		} finally {
			if (exec != null) {
				exec.destroy();
			}
		}
	}

	public static void main(String[] args) {
		try {
			convert("D:\\gifsicle\\26vaPHqaEU0DwiARa.gif", "D:\\gifsicle\\xcc.mp4", 10 ,"mp4");
			System.out.println(ffprobeInfo("D:\\gifsicle\\c.mp4"));
			System.out.println(ffprobeInfo("D:\\gifsicle\\111.gif"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
