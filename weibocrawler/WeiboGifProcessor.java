package com.onemt.news.crawler.dynamicpic.processor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.onemt.news.crawler.dynamicpic.service.CrawlerDynamicPictureService;

/**
 * 
 * 项目名称：crawler-dynamic-picture 
 * 类名称：WeiboGifProcessor 
 * 类描述： 微博抓取gif图片
 * 1.使用HtmlUnitDriver 模拟登录获取登陆后的cookie,模拟访问页面获取数据
 * 
 * 2.当然也可以通过微博api的方式获取,相对比较麻烦,api也有使用次数限制,可以注册多个用户来处理.
 * 
 * 创建人：Administrator 
 * 创建时间：2018年3月5日 下午5:37:55 
 * 修改人：Administrator 
 * 修改时间：2018年3月5日 下午5:37:55 
 * 修改备注：
 * 
 * @version
 */

@Component
public class WeiboGifProcessor {

	/*static {
		System.setProperty("webdriver.chrome.driver", "D:\\chromedriver\\chromedriver.exe");
	}*/

	private static final String gifUrl = "http://wx3.sinaimg.cn/large/%s.gif";
	private static final String WEIBO_LOGIN_URL = "https://passport.weibo.cn/signin/login?entry=mweibo&r=http%3A%2F%2Fweibo.cn%2F&backTitle=%CE%A2%B2%A9&vt=";
	private static final Pattern uPattern = Pattern.compile("u=(.*)");
	//TODO 配置微博帐号 
	private static final String loginName = "";
	//TODO 配置微博密码
	private static final String loginPassword = "";
	private static final String WEIBO_URL = "https://weibo.cn/tokiwatsuki";

	private static final ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();

	@Autowired
	private CrawlerDynamicPictureService crawlerDynamicPictureService;

	/**
	 * 
	 * 启动方法
	 * 
	 */
	//@PostConstruct
	public void init() {
		// 由于使用PostConstruct,需独立一个线程防止执行过长阻碍web容器启动
		newSingleThreadExecutor.execute(() -> {
			Map<String, String> sinaCookie = getSinaCookie();
			crawlGif(WEIBO_URL, sinaCookie);
		});
	}
	

	/**
	 * 获取新浪微博的cookie，这个方法针对weibo.cn有效，对weibo.com无效 weibo.cn以明文形式传输数据
	 * loginName 新浪微博用户名
	 * loginPassword  新浪微博密码
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getSinaCookie() {
		HtmlUnitDriver driver = new HtmlUnitDriver();
		driver.setJavascriptEnabled(true);
		driver.get(WEIBO_LOGIN_URL);

		// 睡几秒,以免太快加载问题
		sleepTime();

		WebElement mobile = driver.findElementByCssSelector("input[id=loginName]");
		mobile.sendKeys(loginName);
		WebElement pass = driver.findElementByCssSelector("input[id=loginPassword]");
		pass.sendKeys(loginPassword);
		WebElement submit = driver.findElementByCssSelector("a[id=loginAction]");
		submit.click();

		// 睡几秒,以免太快加载问题
		sleepTime();

		Set<Cookie> cookieSet = driver.manage().getCookies();
		driver.close();

		Map<String, String> cookieMap = new HashMap<String, String>();
		for (Cookie cookie : cookieSet) {
			cookieMap.put(cookie.getName(), cookie.getValue());
		}
		return cookieMap;
	}

	public void crawlGif(String url, Map<String, String> sinaCookie) {
		Document document = getDocument(url, sinaCookie);
		if (document != null) {
			document.select("a:contains(原图)").eachAttr("href").stream().forEach(href -> {
				Matcher matcher = uPattern.matcher(href);
				if (matcher.find()) {
					String group = matcher.group(1);
					String sourceUrl = String.format(gifUrl, group);
					System.out.println(sourceUrl);
					crawlerDynamicPictureService.saveCrawlerDynamicPicture(sourceUrl);
				}
			});

			Element nextPage = document.selectFirst("#pagelist a:contains(下页)");
			if (nextPage != null) {
				String nextPageUrl = nextPage.absUrl("href");
				crawlGif(nextPageUrl, sinaCookie);
			}
		}
	}

	
	

	public Document getDocument(String url, Map<String, String> sinaCookie) {
		Document document = null;
		try {
			document = Jsoup.connect(url).cookies(sinaCookie)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36")
					.followRedirects(true).ignoreContentType(true).timeout(80000).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return document;
	}

	/**
	 * 
	 * 睡几秒,以免太快加载问题
	 * 
	 */
	public void sleepTime() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
