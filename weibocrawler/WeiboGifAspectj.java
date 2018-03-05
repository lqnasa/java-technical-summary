package com.onemt.news.crawler.dynamicpic.aspectj;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class WeiboGifAspectj {
	private static final Logger logger = LoggerFactory.getLogger(WeiboGifAspectj.class);

	private static final String storagedUrls = "crawler:weibo:storaged_urls";
	
	@Resource(name = "jedisTemplate")
	private SetOperations<String, String> setOperations;
	
	@Pointcut("execution(* com.onemt.news.crawler.dynamicpic.service.CrawlerDynamicPictureService.saveCrawlerDynamicPicture(java.lang.String))&& args(sourceUrl)")
	public void pointCut(String sourceUrl) {
	}

	@Around(value = "pointCut(sourceUrl)", argNames = "sourceUrl")
	public void around(ProceedingJoinPoint joinPoint, String sourceUrl){
		if (setOperations.add(storagedUrls, sourceUrl) == 0) {
			System.out.println("================= around pointCut 该gif Url:"+sourceUrl+" 已经入库,执行过滤.==================");
			return;
		}
		try {
			joinPoint.proceed();
		} catch (Throwable e) {
			e.printStackTrace();
			setOperations.remove(storagedUrls, sourceUrl);
			System.out.println("================= pointCut 该gif Url:"+sourceUrl+" 入库异常,移除redis中已记入状态.============= ");
		}
	}

}
