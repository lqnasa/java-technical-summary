package com.onemt.news.crawler.recycle.kafka;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

/**
 * kafkaProducer监听器，在producer配置文件中开启
 * 
 * @author
 * 
 */

@Component
public class KafkaProducerListener implements ProducerListener<String, String> {
	protected final Logger LOG = LoggerFactory.getLogger("kafkaProducer");

	/**
	 * 发送消息成功后调用
	 */
	@Override
	public void onSuccess(String topic, Integer partition, String key,String value, RecordMetadata recordMetadata) {
		LOG.info("==========kafka发送数据成功（日志开始）==========");
		LOG.info("----------topic:" + topic);
		LOG.info("----------partition:" + partition);
		LOG.info("----------key:" + key);
		LOG.info("----------value:" + value);
		LOG.info("----------RecordMetadata:" + recordMetadata);
		LOG.info("~~~~~~~~~~kafka发送数据成功（日志结束）~~~~~~~~~~");

	}

	/**
	 * 方法返回值代表是否启动kafkaProducer监听器
	 */
	@Override
	public void onError(String topic, Integer partition, String key,String value, Exception exception) {
		LOG.info("==========kafka发送数据错误（日志开始）==========");
		LOG.info("----------topic:" + topic);
		LOG.info("----------partition:" + partition);
		LOG.info("----------key:" + key);
		LOG.info("----------value:" + value);
		LOG.info("----------Exception:" + exception);
		LOG.info("~~~~~~~~~~kafka发送数据错误（日志结束）~~~~~~~~~~");
		exception.printStackTrace();
	}

	/**
	 * 方法返回值代表是否启动kafkaProducer监听器
	 */
	@Override
	public boolean isInterestedInSuccess() {
		LOG.info("///kafkaProducer监听器启动///");
		return true;
	}

}