package com.onemt.news.crawler.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.onemt.news.crawler.common.utils.StringUtils;
import com.onemt.news.crawler.quartz.entity.ScheduleJobInfo;
import com.onemt.news.crawler.quartz.service.QuartzScheduleService;
import com.onemt.news.crawler.web.exception.BusinessException;

@Controller
@RequestMapping("/task")
public class TaskController {

	@Autowired
	private QuartzScheduleService quartzScheduleService;
	
	private static final String REGEX="^\\d+(,\\d+)*$";
	
	/*private static final String JOB_GROUP="DEFAULT";
	
	private static final String JOB_GROUP_TEST="DEFAULT_TEST";*/
	
	/**
	 * 
	 * @Title: getScheduledJobList
	 * @Description: 获取已经运行结束的任务列表
	 * @return
	 */
	@RequestMapping("/getScheduledJobList")
	@ResponseBody
	public List<ScheduleJobInfo> getScheduledJobList(@RequestBody(required=false) ScheduleJobInfo requestScheduleJobInfo){
		String jobName = requestScheduleJobInfo.getJobName();
		String jobGroup = requestScheduleJobInfo.getJobGroup();
		List<ScheduleJobInfo> scheduledJobList = new ArrayList<>();
		if(StringUtils.isNotBlank(jobName)){
			ScheduleJobInfo scheduleJobInfo = quartzScheduleService.getJob(jobName, jobGroup);
			if(scheduleJobInfo==null)
				return scheduledJobList;
			
			scheduledJobList.add(scheduleJobInfo);
		}else{
			scheduledJobList = quartzScheduleService.getScheduledJobList(jobGroup);
		}
		
		return scheduledJobList;
	}
	
	/**
	 * 
	 * @Title: getRunningJobList
	 * @Description: 获取正在运行的任务列表
	 * @return
	 */
	@RequestMapping("/getRunningJobList")
	@ResponseBody
	public List<ScheduleJobInfo> getRunningJobList(String jobName,String jobGroup){
		
		List<ScheduleJobInfo> scheduledJobList = new ArrayList<>();
		if(StringUtils.isNotBlank(jobName)){
			ScheduleJobInfo scheduleJobInfo = quartzScheduleService.getJob(jobName, jobGroup);
			if(scheduleJobInfo==null)
				return scheduledJobList;
			
			scheduledJobList.add(scheduleJobInfo);
		}else{
			scheduledJobList = quartzScheduleService.getRunningJobList(jobGroup);
		}
		
		return scheduledJobList;
		
	}
	
	/**
	 * 
	 * @Title: addJob
	 * @Description: 新增定时任务
	 * @param scheduleJobInfo jobGroup不设置便于 根据jobName查找
	 * @return
	 */
	@RequestMapping(value="/addJob")
	@ResponseBody
	public Map<String,Object> addJob(@RequestBody ScheduleJobInfo scheduleJobInfo){
		Map<String,Object> map=new HashMap<>();
		//cron表达式不合法
		if(!CronExpression.isValidExpression(scheduleJobInfo.getCronExpression())){
			throw new BusinessException("cron表达式无效!");
		}
		
		String type = scheduleJobInfo.getType();
		switch (type) {
		case "1":
			if(!scheduleJobInfo.getMediaIds().matches(REGEX)){
				throw new BusinessException("mediaIds不符合格式!");
			}
			
			if(!scheduleJobInfo.getCategoryIds().matches(REGEX)){
				throw new BusinessException("categoryIds不符合格式!");
			}
			break;
		case "2":
			if(!scheduleJobInfo.getMediaCategoryIds().matches(REGEX)){
				throw new BusinessException("mediaCategoryIds不符合格式!");
			}
			break;
		case "3":
			String[] split = scheduleJobInfo.getMediaCategoryUrls().split("(\n)+");
			List<Integer> errorLines=new ArrayList<>(); 
			for (int i=0;i<split.length;i++) {
				if(!Pattern.matches("^\\d+,http(s)?.*", split[i])){ 
					errorLines.add(i+1);
				}
			}
			if(!errorLines.isEmpty()){
				String join = StringUtils.join(errorLines, ",");
				throw new BusinessException(String.format("抓取内容第[%s]行不合法", join));
			}
			break;
		default:
			break;
		}
		
		boolean isSuccess = quartzScheduleService.addJob(scheduleJobInfo);
		if(!isSuccess)
			throw new BusinessException("定时任务创建失败!");
		map.put("message", "定时任务创建成功!");
		
		return map;
	}
	
	/**
	 * 
	 * @Title: addSimpleJob   注:持久化数据库,simpleTrigger执行完会被删除,因此测试不用该方法处理.
	 * @Description: 新增简单定时任务,用于测试
	 * @param scheduleJobInfo
	 * @return
	 */
	@RequestMapping("/addSimpleJob")
	@ResponseBody
	public Map<String,Object> addSimpleJob(@RequestBody ScheduleJobInfo scheduleJobInfo){
		Map<String,Object> map=new HashMap<>();
		boolean isSuccess = quartzScheduleService.addSimpleJob(scheduleJobInfo);
		if(!isSuccess)
			throw new BusinessException("简单定时任务创建失败!");
		map.put("message", "简单定时任务创建成功!");
		return map;
	}
	
	
	/**
	 * 
	 * @Title: updateJob
	 * @Description: 更新定时任务
	 * @param scheduleJobInfo
	 * @return
	 */
	@RequestMapping("/updateJob")
	@ResponseBody
	public Map<String,Object> updateJob(@RequestBody ScheduleJobInfo scheduleJobInfo){
		Map<String,Object> map=new HashMap<>();
		if(!CronExpression.isValidExpression(scheduleJobInfo.getCronExpression())){
			throw new BusinessException("cron表达式无效!");
		}
		
		String type = scheduleJobInfo.getType();
		switch (type) {
		case "1":
			if(!scheduleJobInfo.getMediaIds().matches(REGEX)){
				throw new BusinessException("mediaIds不符合格式!");
			}
			
			if(!scheduleJobInfo.getCategoryIds().matches(REGEX)){
				throw new BusinessException("categoryIds不符合格式!");
			}
			break;
		case "2":
			if(!scheduleJobInfo.getMediaCategoryIds().matches(REGEX)){
				throw new BusinessException("mediaCategoryIds不符合格式!");
			}
			break;
		case "3":
			String[] split = scheduleJobInfo.getMediaCategoryUrls().split("(\n)+");
			List<Integer> errorLines=new ArrayList<>(); 
			for (int i=0;i<split.length;i++) {
				if(!Pattern.matches("^\\d+,http(s)?.*", split[i])){ 
					errorLines.add(i+1);
				}
			}
			if(!errorLines.isEmpty()){
				String join = StringUtils.join(errorLines, ",");
				throw new BusinessException(String.format("抓取内容第[%s]行不合法", join));
			}
			break;
		default:
			break;
		}
		
		boolean isSuccess = quartzScheduleService.updateJob(scheduleJobInfo);
		if(!isSuccess)
			throw new BusinessException("更新定时任务失败!");
		map.put("message", "更新定时任务成功!");
		 return map;
	}
	
	
	/**
	 * 
	 * @Title: updateSimpleJob  注:持久化数据库,simpleTrigger执行完会被删除,因此测试不用该方法处理.
	 * @Description: 更新简单定时任务,重新执行一次
	 * @param scheduleJobInfo
	 * @return
	 */
	@RequestMapping("/updateSimpleJob")
	@ResponseBody
	public Map<String,Object> updateSimpleJob(@RequestBody ScheduleJobInfo scheduleJobInfo){
		Map<String,Object> map=new HashMap<>();
		boolean isSuccess = quartzScheduleService.updateSimpleJob(scheduleJobInfo);
		if(!isSuccess)
			throw new BusinessException("简单定时任务更新失败!");
		map.put("message","简单定时任务更新成功!");
		return map;
	}
	
	
	/**
	 * 
	 * @Title: deleteJob
	 * @Description: 删除定时任务
	 * @param scheduleJobInfo
	 * @return
	 */
	@RequestMapping("/deleteJob")
	@ResponseBody
	public Map<String,Object> deleteJob(@RequestBody ScheduleJobInfo requestScheduleJobInfo){
		Map<String,Object> map=new HashMap<>();
		//使用默认group
		boolean isSuccess = quartzScheduleService.deleteJob(requestScheduleJobInfo.getJobName(), requestScheduleJobInfo.getJobGroup());
		if(!isSuccess)
			throw new BusinessException("定时任务不存在或者已删除!");
		map.put("message","定时任务删除成功!");
		return map;
	}
	
	
	/**
	 * 
	 * @Title: deleteJob
	 * @Description: 获取定时任务
	 * @param scheduleJobInfo
	 * @return
	 */
	@RequestMapping("/getJob")
	@ResponseBody
	public ScheduleJobInfo getJob(@RequestBody ScheduleJobInfo requestScheduleJobInfo){
		ScheduleJobInfo scheduleJobInfo = quartzScheduleService.getJob(requestScheduleJobInfo.getJobName(), requestScheduleJobInfo.getJobGroup());
		if(scheduleJobInfo==null)
			throw new BusinessException("该定时任务已经不存在!");
		return scheduleJobInfo;
	}
	
	
	/**
	 * 
	 * @Title: pauseJob
	 * @Description: 暂停定时任务
	 * @param scheduleJobInfo
	 * @return
	 */
	@RequestMapping("/pauseJob")
	@ResponseBody
	public Map<String,Object> pauseJob(@RequestBody ScheduleJobInfo requestScheduleJobInfo){
		Map<String,Object> map=new HashMap<>();
		//使用默认group
		boolean isSuccess = quartzScheduleService.pauseJob(requestScheduleJobInfo.getJobName(), requestScheduleJobInfo.getJobGroup());
		if(!isSuccess)
			throw new BusinessException("定时任务暂停失败!");
		
		map.put("message","定时任务暂停成功!");
		
		return map;
	}
	
	/**
	 * 
	 * @Title: resumeJob
	 * @Description: 恢复执行定时任务
	 * @param jobName
	 * @param jobGroup
	 * @return
	 */
	@RequestMapping("/resumeJob")
	@ResponseBody
	public Map<String,Object> resumeJob(@RequestBody ScheduleJobInfo requestScheduleJobInfo){
		Map<String,Object> map=new HashMap<>();
		//使用默认group
		boolean isSuccess = quartzScheduleService.resumeJob(requestScheduleJobInfo.getJobName(), requestScheduleJobInfo.getJobGroup());
		if(!isSuccess)
			throw new BusinessException("定时任务恢复失败!");
		map.put("message", "定时任务恢复执行!");
		return map;
	}
	
	/**
	 * 
	 * @Title: triggerJob
	 * @Description: 立即执行定时任务
	 * @param jobName
	 * @param jobGroup
	 * @return
	 */
	@RequestMapping("/triggerJob")
	@ResponseBody
	public Map<String,Object> triggerJob(@RequestBody ScheduleJobInfo requestScheduleJobInfo) {
		Map<String,Object> map=new HashMap<>();
		// 使用默认group
		boolean isSuccess = quartzScheduleService.triggerJob(requestScheduleJobInfo.getJobName(), requestScheduleJobInfo.getJobGroup());
		if(!isSuccess)
			throw new BusinessException("定时任务执行失败!");
		map.put("message", "定时任务执行成功!");
		return map;
	}
	
	/**
	 * 
	 * @param jobName
	 * @param jobGroup
	 * @return
	 */
	@RequestMapping("/checkJobName")
	@ResponseBody
	public Map<String,Object> checkJobName(String jobName,String jobGroup){
		Map<String,Object> map=new HashMap<>();
		// 使用默认group
		boolean isSuccess = quartzScheduleService.checkJobName(jobName, jobGroup);
		if(isSuccess)
			throw new BusinessException("任务名称已存在!");
		map.put("message", "任务名称可用!");
		return map;
	}
	
	/**
	 * 
	 * @param jobName
	 * @param jobGroup
	 * @return
	 */
	@RequestMapping("/isValidExpression")
	@ResponseBody
	public Map<String,Object> isValidExpression(String cronExpression){
		Map<String,Object> map=new HashMap<>();
		if(!CronExpression.isValidExpression(cronExpression)){
			throw new BusinessException("cron表达式无效!");
		}
		map.put("message", "cron表达式正确!");
		return map;
	}
	
	/**
	 * 
	 * @param jobName
	 * @param jobGroup
	 * @return
	 */
	@RequestMapping("/isValidMediaCategoryUrls")
	@ResponseBody
	public Map<String,Object> isValidUrl(String mediaCategoryUrls){
		Map<String,Object> map=new HashMap<>();
		String[] split = mediaCategoryUrls.split("(\n)+");
		
		List<Integer> errorLines=new ArrayList<>(); 
		for (int i=0;i<split.length;i++) {
			if(!Pattern.matches("^\\d+,http(s)?.*", split[i])){ 
				errorLines.add(i+1);
			}
		}
		
		if(!errorLines.isEmpty()){
			String join = StringUtils.join(errorLines, ",");
			throw new BusinessException(String.format("第[%s]行不合法", join));
		}
		
		map.put("message", "抓取内容配置合法!");
		return map;
	}
	
	
}
