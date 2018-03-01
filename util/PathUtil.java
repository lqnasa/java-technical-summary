package com.onemt.news.crawler.dynamicpic.util;

import java.io.File;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @ClassName: PathUtil
 * @Description: 地址路径工具类
 * @author: yanghaipeng
 * @date: 2017年11月01日 am 8:00:00
 * @version: V1.0
 */
public class PathUtil {

    private static DateTimeFormatter URL_PATH_FORMAT_PATTERN = DateTimeFormat.forPattern("yyyy/M_d/H");
    private static DateTimeFormatter FILE_PATH_FORMAT_PATTERN = DateTimeFormat.forPattern("yyyyMMdd");

    // URL路径分割符号
    public static final String URL_SEPARATOR = "/";
    public static final String URL_FILE_NAME_SUFFIX_POINT = ".";

    /**
     * 生成url路径
     * 
     * 格式：businessName/2012(年)/1-1(月日)/1(小时)/fileFullName, 例子: img/2017/11_1/18/3faf828cd13248febcc36d656fc82226.webp
     * 
     * @param business 具体业务名称(统一为小写),例如:软件升级补丁业务就为apkpatch
     * @param name 文件名
     * @param type 文件后缀,例如:webp
     * 
     * @return
     */
    public static String generateUrlPath(String business, String name, String type) throws Exception {
        if (StringUtils.isBlank(business)) {
            throw new Exception("Generate url fail,business is null");
        }
        if (StringUtils.isBlank(type)) {
            throw new Exception("Generate url fail,type is null");
        }
        if (StringUtils.isBlank(name)) {
            throw new Exception("Generate url fail,name is null");
        }

        StringBuilder url = new StringBuilder();
        url.append(Constants.s3Path);
        url.append(URL_SEPARATOR);

        url.append(business);
        url.append(URL_SEPARATOR);

        url.append(createDynamicPath());
        url.append(URL_SEPARATOR);

        url.append(name);
        url.append(URL_FILE_NAME_SUFFIX_POINT);
        url.append(type);

        return url.toString();
    }

    /**
     * 创建动态路径 规则:年/月_日/小时,例如：2012/8_21/13
     * 
     * @return
     */
    public static String createDynamicPath() throws Exception {
        String dynamicPath = URL_PATH_FORMAT_PATTERN.print(System.currentTimeMillis());

        return dynamicPath;
    }

    /**
     * 生成文件名
     * 
     * @param fileSuffix 后缀
     * @return
     */
    public static String generateFileName(String fileSuffix) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(UUID.randomUUID().toString().replace("-", "").toLowerCase());
        fileName.append(fileSuffix);

        return fileName.toString();
    }

    /**
     * 通过Url地址获得文件名
     * 
     * @param filePath
     */
    public static String getUrlFileName(String urlPath) {
        String fileName = "";

        if (!StringUtils.isEmpty(urlPath)) {
            int index = urlPath.lastIndexOf(URL_SEPARATOR);

            if (index != -1) {
                fileName = urlPath.substring(index + 1, urlPath.length());
            }
        }

        return fileName;
    }

    /**
     * 获得缩略图的路径+文件名
     * 
     * @param path 路径
     * @param w 宽
     * @param h 高
     * 
     */
    public static String getScaledUrlPath(String path, int w, int h) throws Exception {
        int index = path.lastIndexOf(URL_SEPARATOR);
        String first = path.substring(0, index + 1);
        String second = path.substring(index + 1);

        StringBuilder scaledUrlPath = new StringBuilder();
        scaledUrlPath.append(first).append(w).append("_").append(h).append("_").append(second);

        return scaledUrlPath.toString();
    }

    /**
     * 替换文件后缀名
     * 
     * @param name 被替换的对象
     * @param newSuffix 新的文件后缀名
     * @return
     */
    public static String reSuffix(String name, String newSuffix) throws Exception {
        if (name.lastIndexOf(".") == -1) {

            return name + newSuffix;

        } else {
            String partName = name.substring(0, name.lastIndexOf(".") + 1);

            return partName + newSuffix;
        }
    }

    /**
     * 根据时间获取当天的文件夹路径，没有的话创建
     * 
     * @return
     */
    public static String getTodayFilePath() {
        StringBuilder todayFilePath = new StringBuilder();
        todayFilePath.append(Constants.articleImagePath);
        todayFilePath.append(File.separator);
        todayFilePath.append(FILE_PATH_FORMAT_PATTERN.print(System.currentTimeMillis()));
        todayFilePath.append(File.separator);

        File file = new File(todayFilePath.toString());
        if (!file.exists()) {
            file.mkdirs();
        }

        return todayFilePath.toString();
    }

    public static void main(String args[]) throws Exception {
        System.out.println(getTodayFilePath());
    }
}
