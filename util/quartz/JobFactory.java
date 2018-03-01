package com.onemt.news.crawler.quartz.factory;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;
/**
 * 
 * 项目名称：crawler-recycle   
 *
 * 类描述：实现spring管理的bean注入job中.
 * 类名称：com.onemt.news.crawler.recycle.quartz.factory.JobFactory     
 * 创建人：liqiao 
 * 创建时间：2017-5-31 下午4:14:04   
 * 修改人：
 * 修改时间：2017-5-31 下午4:14:04   
 * 修改备注：   
 * @version   V1.0
 */
@Component
public class JobFactory extends AdaptableJobFactory {

	@Autowired
	private AutowireCapableBeanFactory capableBeanFactory;

	@Override
	protected Object createJobInstance(TriggerFiredBundle bundle)
			throws Exception {
		// 调用父类的方法
		Object jobInstance = super.createJobInstance(bundle);
		// 进行注入
		capableBeanFactory.autowireBean(jobInstance);
		return jobInstance;
	}

}