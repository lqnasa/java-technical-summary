package com.onemt.news.crawler.web.shiro.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 
 * 项目名称：crawler-web 
 * 类名称：CheckTokenIsvalidFilter 
 * 类描述： uap token是否失效
 * 创建人：Administrator 
 * 创建时间：2017年8月22日 下午4:15:55 
 * 修改人：Administrator 
 * 修改时间：2017年8月22日 下午4:15:55 
 * 修改备注：
 * 
 * @version
 */
public class CheckTokenIsvalidFilter extends OncePerRequestFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(CheckTokenIsvalidFilter.class);
	
	private String notFilterStr;

	public void setNotFilterStr(String notFilterStr) {
		this.notFilterStr = notFilterStr;
	}


	/**
	 * 判断是否为Ajax请求
	 * 
	 * @param request
	 * @return 是true, 否false
	 * @see [类、类#方法、类#成员]
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		String header = request.getHeader("X-Requested-With");
		if (header != null && "XMLHttpRequest".equals(header))
			return true;
		else
			return false;
	}


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
				// 不过滤的uri
				//String[] notFilter = new String[] { "/cas","/session_invalid","/logout","/static/"};
				HttpServletRequest httpRequest = (HttpServletRequest) request;
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				
				 // 请求的uri  
		        String uri = request.getServletPath();
		        // 是否过滤  
				boolean doFilter = true;
				if(StringUtils.isNotBlank(notFilterStr)){
					for (String s : notFilterStr.split(",")) {
						if (uri.indexOf(s) != -1) {
							// 如果uri中包含不过滤的uri，则不进行过滤
							doFilter = false;
							break;
						}
					}
				}
		
				logger.info(uri);
			
				if (doFilter) {
					// 执行过滤
					// 从session中获取登录者实体
					HttpSession session = httpRequest.getSession();
					
					Object casToken = session.getAttribute("casToken");
					if (null == casToken) {
						if("/index.html".equals(uri)){
							httpResponse.sendRedirect("logout");
							return;
						}
						if (isAjaxRequest(httpRequest)) {
							request.getRequestDispatcher("/ajaxRelogin").forward(request, response);
							return;
						}
						request.getRequestDispatcher("/relogin").forward(request, response);
					} else {
						// 如果session中存在登录者实体，则继续
						filterChain.doFilter(request, response);
					}
				} else {
					// 如果不执行过滤，则继续
					filterChain.doFilter(request, response);
				}
		
	}


}
