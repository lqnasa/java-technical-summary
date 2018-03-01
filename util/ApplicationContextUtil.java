package com.onemt.contentcenter.swarm.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 手动获取Spring上下文和Bean对象
 *
 * @Author YinWenBing
 * @Date 2017/1/6  17:07
 */
@Component
public class ApplicationContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 加载Spring配置文件时，如果Spring配置文件中所定义的Bean类实现了ApplicationContextAware接口，会自动调用该方法
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    	ApplicationContextUtil.applicationContext = applicationContext;
    }

    /**
     * 获取Spring上下文
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取Spring Bean
     * @param name
     * @return
     * @throws BeansException
     */
    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }
    
    public static void autowireBeanProperties(Object thiz){
    	ApplicationContextUtil.getApplicationContext().getAutowireCapableBeanFactory().autowireBeanProperties(thiz, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    }
}