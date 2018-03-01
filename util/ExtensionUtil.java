package com.onemt.news.crawler.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;

public class ExtensionUtil {
	
	private static Map<String, String> extMap = new HashMap<String, String>();

	static {
		extMap.put("application/x-jpg", ".jpg");
		extMap.put("image/jpeg", ".jpg");
		extMap.put("application/x-png", ".png");
		extMap.put("image/png", ".png");
		extMap.put("image/tiff", ".tif");
		extMap.put("image/gif", ".gif");
		extMap.put("application/x-bmp", ".bmp");
		extMap.put("image/x-icon", ".ico");
		extMap.put("application/x-ico", ".ico");
	}

	public static String getExt(String contentType, URL url) {
		String imageType = "";
		if (StringUtils.isNotBlank(contentType)) {
			imageType = extMap.get(contentType.trim().toLowerCase());
		}

		if (StringUtils.isBlank(imageType)) {
			byte[] b = new byte[28];
			InputStream inputStream = null;
			CloseableHttpClient httpClient=null;
			CloseableHttpResponse response =null;
			try {
				httpClient= HttpClientUtil.getHttpClient();
				HttpGet httpGet = new HttpGet(url.toString());
				response = httpClient.execute(httpGet);
				
				if(response.getEntity() != null){
					inputStream =response.getEntity().getContent();
					inputStream.read(b, 0, 28);
				}
				
				StringBuilder stringBuilder = new StringBuilder();
				if (b == null || b.length <= 0) {
					return null;
				}
				
				for (int i = 0; i < b.length; i++) {
					int v = b[i] & 0xFF;
					String hv = Integer.toHexString(v);
					if (hv.length() < 2) {
						stringBuilder.append(0);
					}
					stringBuilder.append(hv);
				}

				FileType fileType = FileType.getFileTypeByValue(stringBuilder.toString());
				if(fileType == null)
					return imageType;
				
				switch (fileType) {
				case JPEG:
					imageType = ".jpg";
					break;
				case PNG:
					imageType = ".png";
					break;
				case GIF:
					imageType = ".gif";
					break;
				case TIFF:
					imageType = ".tiff";
					break;
				case BMP:
					imageType = ".bmp";
					break;
				default:
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (inputStream != null)
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				
				 HttpClientUtils.closeQuietly(response);
				 HttpClientUtils.closeQuietly(httpClient);
			}
		}

		return imageType;
	}
}
