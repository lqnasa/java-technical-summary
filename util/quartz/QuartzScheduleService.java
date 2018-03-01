package com.onemt.news.crawler.quartz.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.onemt.news.crawler.common.utils.StringUtils;
import com.onemt.news.crawler.quartz.entity.Constant;
import com.onemt.news.crawler.quartz.entity.ScheduleJobInfo;
import com.onemt.news.crawler.quartz.factory.QuartzJobFactory;
import com.onemt.news.crawler.recycle.redis.CrawlerRedisDataDao;

/**
 * 
 * 项目名称：crawler-recycle   
 *
 * 类描述：
 * 类名称：com.onemt.news.crawler.recycle.quartz.service.QuartzScheduleService     
 * 创建人：liqiao 
 * 创建时间：2017-6-6 上午10:18:45   
 * 修改人：
 * 修改时间：2017-6-6 上午10:18:45   
 * 修改备注：   
 * @version   V1.0
 */
@Service
public class QuartzScheduleService {

    private static Logger logger = LoggerFactory.getLogger(QuartzScheduleService.class);

    @Autowired
    private Scheduler scheduler;
    
    @Autowired
    private CrawlerRedisDataDao CrawlerRedisDataDao;
    
    /**
     * 添加一个Job
     *
     * @param job
     */
    public boolean addJob(ScheduleJobInfo job) {
        try {
            //获取TriggerKey
            TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
            //从数据库中查询触发器
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            //不存在则新建一个触发器
            if (null == trigger) {
                JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class)
                        .withIdentity(job.getJobName(), job.getJobGroup())
                        .build();
               
                //表达式调度构建器
                CronScheduleBuilder cronBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
                //按新的cronExpression表达式构建一个新的trigger
                trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup()).withSchedule(cronBuilder).withDescription(job.getDescription()).build();
                //将ScheduleJobInfo对象序列化到数据库中,其中包含需要获取的分类和子分类信息.传递给job调用.
                trigger.getJobDataMap().put(Constant.JOB_PARAM_KEY, job);
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                // Trigger已存在，那么更新相应的定时设置
                //表达式调度构建器
                CronScheduleBuilder cronBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
                //按新的cronExpression表达式重新构建trigger
                trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(cronBuilder).withDescription(job.getDescription()).build();
                //将ScheduleJobInfo对象序列化到数据库中,其中包含需要获取的分类和子分类信息.传递给job调用.
                trigger.getJobDataMap().put(Constant.JOB_PARAM_KEY, job);
                //按新的trigger重新设置job执行
                scheduler.rescheduleJob(triggerKey, trigger);
            }
            return true;
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error(e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 添加一个测试使用的job,只执行一次任务
     * 
     * 注:持久化数据库,simpleTrigger执行完会被删除,因此测试不用该方法处理.
     *
     * @param job
     */
    public boolean addSimpleJob(ScheduleJobInfo job) {
        try {
            //获取TriggerKey
            TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
            //从数据库中查询触发器
            SimpleTrigger trigger =  (SimpleTrigger) scheduler.getTrigger(triggerKey);
            //不存在则新建一个触发器
            if (null == trigger) {
                JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class)
                        .withIdentity(job.getJobName(), job.getJobGroup())
                        .build();
               
                //按新的cronExpression表达式构建一个新的trigger
                trigger = TriggerBuilder.newTrigger()
                		.withIdentity(job.getJobName(), job.getJobGroup())
                		.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24*365*10).repeatForever())//永远重复,将重复时间设置10年
                		.withDescription(job.getDescription()).build();
                //将ScheduleJobInfo对象序列化到数据库中,其中包含需要获取的分类和子分类信息.传递给job调用.
                trigger.getJobDataMap().put(Constant.JOB_PARAM_KEY, job);
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                // Trigger已存在，那么更新相应的定时设置
                //按新的cronExpression表达式重新构建trigger
                trigger = (SimpleTrigger) trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24*365*10).repeatForever()).withDescription(job.getDescription()).build();
                //将ScheduleJobInfo对象序列化到数据库中,其中包含需要获取的分类和子分类信息.传递给job调用.
                trigger.getJobDataMap().put(Constant.JOB_PARAM_KEY, job);
                //按新的trigger重新设置job执行
                scheduler.rescheduleJob(triggerKey, trigger);
            }
            
            //简单定时任务用于测试,则需清空redis缓存
            CrawlerRedisDataDao.removeRedisData();
            
            return true;
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error(e.getMessage(), e);
            return false;
        }
    }


    /**
     * 更新一个job
     *
     * @param jobInfo
     * @return
     */
    public boolean updateJob(ScheduleJobInfo jobInfo) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getJobName(), jobInfo.getJobGroup());
            //获取trigger
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            //表达式调度构建器
            CronScheduleBuilder cronBuilder = CronScheduleBuilder.cronSchedule(jobInfo.getCronExpression());
            //按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withDescription(jobInfo.getDescription())
                    .withSchedule(cronBuilder).build();
            //更新传参
            trigger.getJobDataMap().put(Constant.JOB_PARAM_KEY, jobInfo);
            //按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }
    }

    
    /**
     * 更新一个job
     * 
     * 注:持久化数据库,simpleTrigger执行完会被删除,因此测试不用该方法处理.
     *
     * @param jobInfo
     * @return
     */
    public boolean updateSimpleJob(ScheduleJobInfo jobInfo) {
        try {
        	
        	//简单定时任务用于测试,则需清空redis缓存
            CrawlerRedisDataDao.removeRedisData();
        	
            TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getJobName(), jobInfo.getJobGroup());
            //获取trigger
            SimpleTrigger trigger = (SimpleTrigger) scheduler.getTrigger(triggerKey);
            
            trigger.getTriggerBuilder().withIdentity(triggerKey)
            .withDescription(jobInfo.getDescription())
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24*365*10).repeatForever())//永远重复,将重复时间设置10年
            .build();
            trigger.getJobDataMap().put(Constant.JOB_PARAM_KEY, jobInfo);
            //按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
            
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * 获取计划中的job
     *
     * @return
     */
    public List<ScheduleJobInfo> getScheduledJobList(String jobGroup) {
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            List<ScheduleJobInfo> jobList = new ArrayList<ScheduleJobInfo>();
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                	 String group = jobKey.getGroup();
                	 //如果jobGroup不存在则获取全部任务列表,或者根据分组返回任务列表
                	 if(StringUtils.isBlank(jobGroup)||group.equals(jobGroup)){
                		 ScheduleJobInfo job = (ScheduleJobInfo) trigger.getJobDataMap().get(Constant.JOB_PARAM_KEY);
                    	 
                    	 job.setStartTime( trigger.getStartTime());
                    	 job.setEndTime(trigger.getEndTime());
                    	 job.setPreviousFireTime(trigger.getPreviousFireTime());
                    	 job.setNextFireTime(trigger.getNextFireTime());
                    	 job.setPriority(trigger.getPriority());
                    	 /*if (trigger instanceof CronTrigger) {
                             CronTrigger cronTrigger = (CronTrigger) trigger;
                             String cronExpression = cronTrigger.getCronExpression();
                             job.setCronExpression(cronExpression);
                         }*/
                    	 Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                         job.setJobStatus(triggerState.name());
                         jobList.add(job); 
                	 }
                }
            }
            return jobList;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * 获取运行中的任务
     * 分组获取
     * @param String jobGroup
     *
     * @return
     */
    public List<ScheduleJobInfo> getRunningJobList(String jobGroup) {
        try {
            List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
            List<ScheduleJobInfo> jobList = new ArrayList<ScheduleJobInfo>(executingJobs.size());
            
            for (JobExecutionContext executingJob : executingJobs) {
            	 Trigger trigger = executingJob.getTrigger();
            	 JobKey jobKey = trigger.getJobKey();
            	 String group = jobKey.getGroup();
            	 //如果jobGroup不存在则获取全部任务列表,或者根据分组返回任务列表
            	 if(StringUtils.isBlank(jobGroup)||group.equals(jobGroup)){
                	 ScheduleJobInfo job = (ScheduleJobInfo) executingJob.getMergedJobDataMap().get(Constant.JOB_PARAM_KEY);
                	 
                	 job.setStartTime( trigger.getStartTime());
                	 job.setEndTime(trigger.getEndTime());
                	 job.setPreviousFireTime(trigger.getPreviousFireTime());
                	 job.setNextFireTime(trigger.getNextFireTime());
                	 job.setPriority(trigger.getPriority());
                	 /*if (trigger instanceof CronTrigger) {
                         CronTrigger cronTrigger = (CronTrigger) trigger;
                         String cronExpression = cronTrigger.getCronExpression();
                         job.setCronExpression(cronExpression);
                     }*/
                	 Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                     job.setJobStatus(triggerState.name());
                     jobList.add(job); 
            	 }
            }
            return jobList;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * 暂停一个Job
     *
     * @param jobName
     * @param jobGroup
     * @return
     */
    public boolean pauseJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = getJobKey(jobName, jobGroup);
            scheduler.pauseJob(jobKey);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 恢复一个暂停的作业
     * @param jobName
     * @param jobGroup
     * @return
     */
    public boolean resumeJob(String jobName, String jobGroup) {
        try {
            JobKey jobKey = getJobKey(jobName, jobGroup);
            scheduler.resumeJob(jobKey);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }


    /**
     * 根据jobName获取Job
     *
     * @param jobName
     * @param jobGroup
     * @return
     */
    public ScheduleJobInfo getJob(String jobName, String jobGroup) {
        try {
        	TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            ScheduleJobInfo scheduleJobInfo = (ScheduleJobInfo) trigger.getJobDataMap().get(Constant.JOB_PARAM_KEY);
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            scheduleJobInfo.setJobStatus(triggerState.name());
            
            return scheduleJobInfo;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    
    
    /**
     * 删除一个Job
     *
     * @param jobName
     * @param jobGroup
     * @return
     */
    public boolean deleteJob(String jobName, String jobGroup) {
        try {
        	//删除定时任务时   先暂停任务，然后再删除
            JobKey jobKey = getJobKey(jobName, jobGroup);
            scheduler.pauseJob(jobKey);
            scheduler.deleteJob(jobKey);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
    
    
    
    /**
     * 运行一次任务
     * @param jobName
     * @param jobGroup
     * @return
     */
    public boolean triggerJob(String jobName, String jobGroup) {
        try {
        	
        	  //简单定时任务用于测试,则需清空redis缓存
            CrawlerRedisDataDao.removeRedisData();
        	
        	JobKey jobKey = getJobKey(jobName, jobGroup);
        	
        	TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            Trigger trigger = scheduler.getTrigger(triggerKey);
        	
            scheduler.triggerJob(jobKey,trigger.getJobDataMap());
            
            return true;
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 
     * 判定是否可用
     * @param jobName
     * @param jobGroup
     * @return
     */
    public boolean checkJobName(String jobName, String jobGroup) {
    	
    	boolean isUsed=false;
    	
    	JobKey jobKey = getJobKey(jobName, jobGroup);
    	try {
    		isUsed=scheduler.checkExists(jobKey);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
    	return isUsed;
    }


    private JobKey getJobKey(String jobName, String jobGroup) {
        return JobKey.jobKey(jobName, jobGroup);
    }

}
