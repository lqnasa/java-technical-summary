package com.onemt.news.crawler.dynamicpic.util;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
/**
 * 
 * 项目名称：crawler-dynamic-picture
 * 类名称：ImageTools
 * 类描述：  图片处理工具类,图片,动图的压缩工具,添加水印也可以
 * (imageMagick 必须先安装) 缩放,裁切,(可加水印-未实现),压缩
 * 
 * 创建人：Administrator 
 * 创建时间：2018年3月6日 下午4:20:20
 * 修改人：Administrator 
 * 修改时间：2018年3月6日 下午4:20:20
 * 修改备注： 
 * @version
 */
public class ImageTools {

	/**
	 * ImageMagick的路径
	 * 
	 */
	public static final String imageMagickPath = "C:\\Program Files\\ImageMagick-7.0.7-Q16";

	/**
	 * 
	 * 根据坐标裁剪图片
	 * 
	 * @param srcPath
	 *            要裁剪图片的路径
	 * @param newPath
	 *            裁剪图片后的路径
	 * @param x
	 *            起始横坐标
	 * @param y
	 *            起始纵坐标
	 * @param x1
	 *            结束横坐标
	 * @param y1
	 *            结束纵坐标
	 */

	public static void cutImage(String srcPath, String newPath, Integer x, Integer y, Integer x1, Integer y1) throws Exception {
		int width = x1 - x;
		int height = y1 - y;
		IMOperation op = new IMOperation();
		op.addImage(srcPath);
		//width： 裁剪的宽度 height： 裁剪的高度 x： 裁剪的横坐标 y： 裁剪的挫坐标
		op.crop(width, height, x, y);
		op.addImage(newPath);
		ConvertCmd convert = new ConvertCmd();
		// linux下不要设置此值，不然会报错
		setSearchPath(convert);
		convert.run(op);
	}

	/**
	 * 
	 * 根据尺寸缩放图片
	 * 
	 * @param width
	 *            缩放后的图片宽度
	 * @param height
	 *            缩放后的图片高度
	 * @param srcPath
	 *            源图片路径
	 * @param newPath
	 *            缩放后图片的路径
	 */
	public static void cutImage(Integer width, Integer height, String srcPath, String newPath) throws Exception {
		IMOperation op = new IMOperation();
		op.addImage(srcPath);
		op.resize(width, height);
		op.addImage(newPath);
		ConvertCmd convert = new ConvertCmd();
		// linux下不要设置此值，不然会报错
		setSearchPath(convert);
		convert.run(op);

	}
	
	/**
	 * 
	 * 图片压缩  
	 * 
	 * convert srcPath.gif -fuzz 5% -layers Optimize newPath.gif
	 * 
	 * @param srcPath
	 * @param newPath
	 * @throws Exception
	 */
	public static void compressImage(String srcPath, String newPath) throws Exception {
		IMOperation op = new IMOperation();	
		op.addImage(srcPath);
		op.fuzz(5.0,true).layers("Optimize");
		op.addImage(newPath);
		ConvertCmd convert = new ConvertCmd();
		// linux下不要设置此值，不然会报错
		setSearchPath(convert);
		convert.run(op);
	}

	
	public static void setSearchPath(ConvertCmd convert) {
		if(WebToolUtils.isWindowsOS()){
			convert.setSearchPath(imageMagickPath);
		}
	}

	public static void main(String[] args) throws Exception {
		// cutImage("D:\\test.jpg", "D:\\new.jpg", 98, 48, 370,320);
		// cutImage(200,300, "/home/1.jpg", "/home/2.jpg");
		compressImage("D:\\gifsicle\\2.gif","D:\\gifsicle\\1.gif");
		
		
	}
}