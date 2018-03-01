package com.onemt.news.crawler.common.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 
 * 项目名称：crawler-common
 * 类名称：InetAddressUtils
 * 类描述： 获取主机ip地址 ipv4
 * 创建人：Administrator 
 * 创建时间：2017年7月7日 下午4:09:27
 * 修改人：Administrator 
 * 修改时间：2017年7月7日 下午4:09:27
 * 修改备注： 
 * @version
 */
public class InetAddressUtils {

	/**
	 *  获取主机ip地址 ipv4
	 * @return
	 */
	public static String getHostAddress(){
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface current = interfaces.nextElement();
				if (!current.isUp() || current.isLoopback() || current.isVirtual())
					continue;
				Enumeration<InetAddress> addresses = current.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr.isLoopbackAddress())
						continue;
					if(addr !=null && addr instanceof Inet4Address)
						return addr.getHostAddress();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
