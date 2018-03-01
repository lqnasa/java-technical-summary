package com.onemt.news.crawler.web.shiro.realm;

import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cas.CasAuthenticationException;
import org.apache.shiro.cas.CasRealm;
import org.apache.shiro.cas.CasToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.AssertionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.onemt.news.crawler.web.shiro.domain.User;
import com.onemt.news.crawler.web.shiro.util.UapUtil;

/**
 * 
 * 项目名称：crawler-web 类名称：UserRealm 类描述： 用户授权信息域 创建人：Administrator 创建时间：2017年8月22日
 * 下午4:12:47 修改人：Administrator 修改时间：2017年8月22日 下午4:12:47 修改备注：
 * 
 * @version
 */
public class UserRealm extends CasRealm {
	// 打印日志的
	private Logger logger = LoggerFactory.getLogger(UserRealm.class);


	/**
	 * 1、CAS认证 ,验证用户身份
	 * 2、将用户基本信息设置到会话中
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
		
		// 转换为casToken
		CasToken casToken = (CasToken) token;
		
		// 没有casToken的时候，直接打回
		if (casToken == null) {
			return null;
		}
		
		// 获取票据
		String ticket = (String) casToken.getCredentials();
		
		// 如果票据为空的话,与case服务端交互失败,直接打回
		if (!StringUtils.hasText(ticket)) {
			return null;
		}
		

		// 将ticket存入session
		Session session = SecurityUtils.getSubject().getSession();
		session.setAttribute("casTicket", ticket);

		try {
			// 获取cas服务端，并且拿着ticket去和服务端交互
			String clientCaseService = getCasService();

			Map<String, Object> rspdata = UapUtil.ssoServiceValidate(clientCaseService, ticket);
			String returnToken = (String) rspdata.get("token");
			session.setAttribute("casToken", returnToken); // 将casToken存入session

			// 后面写成相应的配置文件形式,uap的登录地址
			// 构建cas验证结果的断言实现类,封装了相应的身份识别
			AssertionImpl casAssertion = new AssertionImpl(returnToken);

			AttributePrincipal casPrincipal = casAssertion.getPrincipal();

			String username = casPrincipal.getName();

			logger.debug("Validate ticket : {} in CAS server : {} to retrieve token : {}",
					new Object[] { ticket, getCasServerUrlPrefix(), returnToken });

			casToken.setUserId(username);
			casToken.setRememberMe(true);

			// create simple authentication info
			User userInfo = UapUtil.getUserInfo();
			// 创建用户授权信息
			return new SimpleAuthenticationInfo(userInfo, ticket, getName()); // token相当于相应的用户信息，票据相当于证书
		} catch (Exception e) {
			session.setAttribute("exception", e.getMessage());
			throw new CasAuthenticationException("Unable to validate ticket [" + ticket + "]", e);
		}

	}

	// /**
	// * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
	// * 现在和uap集成，鉴权的部分由uap来做，所以这里预留做本地鉴权（用作对菜单进行相应的增、删、改、查等权限）
	// */
	// @Override
	// protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection
	// principals) {
	// }

	/**
	 * 清空用户关联权限认证，待下次使用时重新加载
	 */
	public void clearCachedAuthorizationInfo(String principal) {
		SimplePrincipalCollection principals = new SimplePrincipalCollection(principal, getName());
		clearCachedAuthorizationInfo(principals);
	}

	/**
	 * 清空所有关联认证
	 */
	public void clearAllCachedAuthorizationInfo() {
		Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
		if (cache != null) {
			for (Object key : cache.keys()) {
				cache.remove(key);
			}
		}
	}
}
