package com.onemt.news.crawler.common.utils;

import org.apache.commons.codec.digest.DigestUtils;
/**
 * 
 * 项目名称：youtubeCrawler
 * 类名称：ShortUrlUtils
 * 类描述： 短链生成器
 * 创建人：Administrator 
 * 创建时间：2017年11月16日 下午5:33:39
 * 修改人：Administrator 
 * 修改时间：2017年11月16日 下午5:33:39
 * 修改备注： 
 * @version
 */
public class ShortUrlUtils {
	// 可以自定义生成 MD5 盐
	private static final String solt = "onemt";
	// 要使用生成 URL 的字符
	private static final String[] chars = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
			"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6",
			"7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
			"S", "T", "U", "V", "W", "X", "Y", "Z" };

	public static String getshortUrl(String url) {
		StringBuffer resUrl = new StringBuffer();
		String md5Str = DigestUtils.md5Hex(solt + url);
		String sTempSubString = md5Str.substring(8, 16);
		long lHexLong = 0x3FFFFFFF & Long.parseLong(sTempSubString, 16);
		for (int j = 0; j < 6; j++) {
			// 把得到的值与 0x0000003D 进行位与运算，取得字符数组 chars 索引
			long index = 0x0000003D & lHexLong;
			// 把取得的字符相加
			resUrl.append(chars[(int) index]);
			// 每次循环按位右移 5 位
			lHexLong = lHexLong >> 5;
		}
		return resUrl.toString();
	}

	
	public static void main(String[] args) {
		// 长连接
		String longUrl = "http://www.onmet.com";
		// 转换成的短链接后6位码
		String shortCodeArray = getshortUrl(longUrl);
		System.out.println(shortCodeArray);// 任意一个都可以作为短链接码
	}

	
}