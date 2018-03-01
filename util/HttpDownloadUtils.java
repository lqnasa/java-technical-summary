package com.onemt.news.crawler.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onemt.agent.annotation.TraceClass;
import com.onemt.agent.annotation.TraceMethod;

/**
 * 
 * @ClassName: HttpDownloadUtils
 * @Description: http下载网络文件工具类
 * @author lennon dai
 * @date 2016-5-4 下午12:01:11
 *
 */
@TraceClass
public class HttpDownloadUtils {

	private static Logger logger = LoggerFactory.getLogger(HttpDownloadUtils.class);

	private static final String IMAGE_TYPE_REG = "(?i)\\.jpg|\\.jpeg|\\.gif|\\.png|\\.bmp|\\.ico|\\.tif|\\.tiff";

	public static String downLoadFromUrl(String urlStr, String saveFileName, String savePath, String extension)
			throws Exception {

		URL urlfile = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 文件保存位置
		File saveDir = new File(savePath);
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
		// 判定url是否是base64的图片
		Matcher matcher = Pattern.compile("^data:(image/\\w+);base64,(.*?)$").matcher(urlStr);
		String imageType = "";
		String base64Image = "";
		while (matcher.find()) {
			imageType = matcher.group(1);
			base64Image = matcher.group(2);
		}
		// base64图片处理
		if (StringUtils.isNotBlank(imageType)) {

			String webExtension = ExtensionUtil.getExt(imageType, null);
			byte[] decode = Base64.getDecoder().decode(base64Image);
			File f = new File(saveDir + File.separator + saveFileName + webExtension);
			try {
				bos = new BufferedOutputStream(new FileOutputStream(f));
				bos.write(decode, 0, decode.length);
				bos.flush();
				logger.info("Download file image:base64 success:" + f.getName());
				return f.getName();
			} catch (IOException e) {
				logger.error("Download file failed!", e);
				throw e;
			} finally {
				if (bos != null)
					bos.close();
			}

		} else {

			CloseableHttpClient httpClient = null;
			CloseableHttpResponse response = null;
			try {
				urlfile = new URL(clearUrl(urlStr));
				httpClient = HttpClientUtil.getHttpClient();
				HttpGet httpGet = new HttpGet(clearUrl(urlStr));
				httpGet.setHeader("Accept", "*/*");
				response = httpClient.execute(httpGet);
				String contentType = response.getEntity().getContentType() != null
						? response.getEntity().getContentType().getValue() : "";
				String webExtension = ExtensionUtil.getExt(contentType, urlfile);
				if (StringUtils.isNotBlank(webExtension)) {
					extension = webExtension;
				}
				if (!extension.matches(IMAGE_TYPE_REG))
					throw new Exception(String.format("非图片格式,url:%s", urlStr));

				File f = new File(saveDir + File.separator + saveFileName + extension);
				bis = new BufferedInputStream(response.getEntity().getContent());
				bos = new BufferedOutputStream(new FileOutputStream(f));
				int len = 1024 * 10;
				byte[] b = new byte[len];
				while ((len = bis.read(b)) != -1) {
					bos.write(b, 0, len);
				}
				bos.flush();
				bos.close();
				bis.close();
				logger.info("Download file success:" + urlStr);
				return f.getName();
			} catch (Exception e) {
				logger.error("Download file failed!", e);
				throw e;
			} finally {
				try {
					if (null != bis) {
						bis.close();
						bis = null;
					}
					if (null != bos) {
						bos.close();
						bos = null;
					}
					HttpClientUtils.closeQuietly(response);
					HttpClientUtils.closeQuietly(httpClient);
				} catch (IOException e) {
					logger.error("Download file failed!", e);
				}
			}
		}

	}

	/**
	 * 
	 * @Title: getTodayFilePath @Description:
	 *         根据时间获取当天的文件夹路径，没有的话创建 @param @return @return String @throws
	 */
	public static String getTodayFilePath() {
		String todayFilePath = Constants.ARTICLE_IMAGE_PATH + File.separator + DateUtils.getCurrentByFormat("yyyyMMdd")
				+ File.separator;
		File file = new File(todayFilePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		return todayFilePath;
	}

	@TraceMethod
	public static File downLoadFromUrl(String urlStr) throws Exception {

		URL urlfile = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 文件保存位置
		String saveDir = getTodayFilePath();
		String saveFileName = ShortUrlUtils.getshortUrl(urlStr);
		// 判定url是否是base64的图片
		Matcher matcher = Pattern.compile("^data:(image/\\w+);base64,(.*?)$").matcher(urlStr);
		if (matcher.find()) {
			
			String imageType = matcher.group(1);
			String base64Image = matcher.group(2);
			String webExtension = ExtensionUtil.getExt(imageType, null);
			if (!webExtension.matches(IMAGE_TYPE_REG)){
				logger.error(String.format("非图片格式过滤不抓取,url:%s", urlStr));
				return null;
			}
			byte[] decode = Base64.getDecoder().decode(base64Image);
			File f = new File(saveDir + File.separator + saveFileName + webExtension);
			try {
				bos = new BufferedOutputStream(new FileOutputStream(f));
				bos.write(decode, 0, decode.length);
				bos.flush();
				logger.info("Download file image:base64 success:" + f.getName());
				return f;
			} catch (IOException e) {
				//base64的图片处理异常,不做重试,直接捕获
				logger.error("Download file image:base64 failed!(base64的图片处理异常,不做重试,直接捕获,该图片抛弃不抓取!)", e);
				return null;
			} finally {
				if (bos != null){
					try {
						bos.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} else {

			CloseableHttpClient httpClient = null;
			CloseableHttpResponse response = null;
			try {
				urlfile = new URL(clearUrl(urlStr));
				httpClient = HttpClientUtil.getHttpClient();
				HttpGet httpGet = new HttpGet(clearUrl(urlStr));
				httpGet.setHeader("Accept", "*/*");
				response = httpClient.execute(httpGet);
				
				Header header = response.getEntity().getContentType();
				String contentType =  header != null ? header.getValue() : "";
				String webExtension = ExtensionUtil.getExt(contentType, urlfile);
				if (StringUtils.isBlank(webExtension) || !webExtension.matches(IMAGE_TYPE_REG)){
					logger.error(String.format("非图片格式过滤不抓取,url:%s", urlStr));
					return null;
				}
				File f = new File(saveDir + File.separator + saveFileName + webExtension);
				bis = new BufferedInputStream(response.getEntity().getContent());
				bos = new BufferedOutputStream(new FileOutputStream(f));
				int len = 1024 * 10;
				byte[] b = new byte[len];
				while ((len = bis.read(b)) != -1) {
					bos.write(b, 0, len);
				}
				bos.flush();
				logger.info("Download file success:" + urlStr);
				return f;
			} catch (Exception e) {
				logger.error("Download file failed!", e);
				throw e;
			} finally {
					if (null != bis) {
						try {
							bis.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						bis = null;
					}
					if (null != bos) {
						try {
							bos.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						bos = null;
					}
				HttpClientUtils.closeQuietly(response);
				HttpClientUtils.closeQuietly(httpClient);
			}
		}

	}

	public static String clearUrl(String urlStr) {
		try {
			urlStr = NewsURLEncoder.encode(urlStr, "UTF-8");

			if (urlStr.indexOf("%") < 0) {
				URL url = new URL(urlStr);
				String query = url.getQuery();
				if (StringUtils.isNotBlank(query)) {
					// 减一去掉问号
					urlStr = urlStr.substring(0, urlStr.indexOf(query) - 1);
				}
				urlStr = NewsURLEncoder.encode(urlStr, "UTF-8") + (StringUtils.isNotBlank(query) ? "?" + query : "");
			}
		} catch (Exception e) {
			logger.error("Clear url failed!", e);
		}

		return urlStr;
	}

}
