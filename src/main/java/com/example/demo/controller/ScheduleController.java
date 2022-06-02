package com.example.demo.controller;

import com.example.demo.service.QuartzService;
import com.example.demo.vo.JobInfo;
import com.example.demo.vo.UpdateJobInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {
    private final QuartzService quartzService;

    // 현재 동작중인 Job 및 Trigger 리스트 전체 조회
    @GetMapping("/job-list")
    public void jobList() {
        try {
            Scheduler scheduler = quartzService.getSchedulerFactoryBean().getScheduler();
            if(!scheduler.isStarted()) {
                log.info("스케줄러가 동작하고 있지 않습니다.");
                return;
            }

            for(String groupName : scheduler.getJobGroupNames()) {
                System.out.println("* JobGroup : " + groupName);
                for(JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    System.out.println("  - JobName : " + jobKey.getName());
                    for(Trigger trigger : scheduler.getTriggersOfJob(jobKey)) {
                        System.out.println("    > trigger : " + trigger.getKey());
                        System.out.println("      cronExpression : " + trigger.getJobDataMap().get("cronExpression"));
                        System.out.println("      description : " + trigger.getJobDataMap().get("description"));
                        System.out.println("      isScheduled : " + scheduler.getTriggerState(trigger.getKey()));
                    }
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    // 동작중인 스케줄러 일시정지
    @PostMapping("/pause")
    public void pause(@RequestBody JobInfo jobInfo) {
        try {
            Scheduler scheduler = quartzService.getSchedulerFactoryBean().getScheduler();
            if(scheduler.isStarted()) {
                JobKey jobKey = new JobKey(jobInfo.getJobName(), jobInfo.getJobGroupName());
                TriggerKey triggerKey = new TriggerKey(jobInfo.getTriggerName(), jobInfo.getJobGroupName());
                if(scheduler.checkExists(jobKey)) {
                    if("".equals(jobInfo.getTriggerName())) {
                        scheduler.pauseJob(jobKey);
                        log.info("'" + jobKey.getGroup() + "." + jobKey.getName() + "' Job 과 연관된 Trigger 가 모두 Pause 되었습니다.");
                    } else {
                        if(scheduler.checkExists(triggerKey)) {
                            scheduler.pauseTrigger(triggerKey);
                            log.info("'" + triggerKey.getGroup() + "." + triggerKey.getName() + "' Trigger 가 Pause 되었습니다.");
                        } else {
                            log.info("Trigger 가 존재하지 않습니다.");
                        }
                    }
                } else {
                    log.info("Job 이 존재하지 않습니다.");
                }
            } else {
                log.info("스케줄러가 동작하고 있지 않습니다.");
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/resume")
    public void resume(@RequestBody JobInfo jobInfo) {
        try {
            Scheduler scheduler = quartzService.getSchedulerFactoryBean().getScheduler();
            if(scheduler.isStarted()) {
                JobKey jobKey = new JobKey(jobInfo.getJobName(), jobInfo.getJobGroupName());
                TriggerKey triggerKey = new TriggerKey(jobInfo.getTriggerName(), jobInfo.getJobGroupName());
                if(scheduler.checkExists(jobKey)) {
                    if("".equals(jobInfo.getTriggerName())) {
                        scheduler.resumeJob(jobKey);
                        log.info("'" + jobKey.getGroup() + "." + jobKey.getName() + "' Job 과 연관된 Trigger 가 모두 Resume 되었습니다.");
                    } else {
                        if(scheduler.checkExists(triggerKey)) {
                            scheduler.resumeTrigger(triggerKey);
                            log.info("'" + triggerKey.getGroup() + "." + triggerKey.getName() + "' Trigger 가 Resume 되었습니다.");
                        } else {
                            log.info("Trigger 가 존재하지 않습니다.");
                        }
                    }
                } else {
                    log.info("Job 이 존재하지 않습니다.");
                }
            } else {
                log.info("스케줄러가 동작하고 있지 않습니다.");
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/add")
    public void add(@RequestBody JobInfo jobInfo) {
        try {
            Scheduler scheduler = quartzService.getSchedulerFactoryBean().getScheduler();
            JobKey jobKey = new JobKey(jobInfo.getJobName(), jobInfo.getJobGroupName());
            TriggerKey triggerKey = new TriggerKey(jobInfo.getTriggerName(), jobInfo.getJobGroupName());
            boolean jobExistYn = false;

            if(!scheduler.isStarted()) {
                log.info("스케줄러가 동작하고 있지 않습니다.");
                return;
            }
            if(scheduler.checkExists(jobKey)) {
                jobExistYn = true;
            }
            if(scheduler.checkExists(triggerKey)) {
                log.info("JobGroup 내에 동일한 TriggerName 이 존재합니다.");
                return;
            }

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("cronExpression", jobInfo.getCronExpression());
            jobDataMap.put("description", jobInfo.getDescription());

            if(jobExistYn) {
                Trigger trigger = quartzService.cronTrigger(jobInfo.getTriggerName(), jobInfo.getJobGroupName(), jobInfo.getCronExpression(), jobDataMap, jobInfo.getJobName());
                scheduler.scheduleJob(trigger);
                log.info("기존 Job 에 새로운 Trigger 가 둥록되었습니다.");
            } else {
                JobDetail job = quartzService.jobDetail(jobInfo.getClassName(), jobInfo.getJobName(), jobInfo.getJobGroupName());
                Trigger trigger = quartzService.cronTrigger(jobInfo.getTriggerName(), jobInfo.getJobGroupName(), jobInfo.getCronExpression(), jobDataMap, job.getKey().getName());
                scheduler.scheduleJob(job, trigger);
                log.info("새로운 Job 과 Trigger 가 등록되었습니다.");
            }
        } catch (SchedulerException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/remove")
    public void remove(@RequestBody JobInfo jobInfo) {
        try {
            Scheduler scheduler = quartzService.getSchedulerFactoryBean().getScheduler();
            JobKey jobKey = new JobKey(jobInfo.getJobName(), jobInfo.getJobGroupName());

            if(!scheduler.isStarted()) {
                log.info("스케줄러가 동작하고 있지 않습니다.");
                return;
            }
            if(!scheduler.getJobGroupNames().contains(jobInfo.getJobGroupName())) {
                log.info("JobGroup 이 존재하지 않습니다.");
                return;
            }
            if(!scheduler.checkExists(jobKey)) {
                log.info("JobName 이 존재하지 않습니다.");
                return;
            }

            if("".equals(jobInfo.getTriggerName())) {
                scheduler.deleteJob(jobKey);
                log.info("Job 및 연관된 Trigger 가 모두 제거되었습니다.");
            } else {
                TriggerKey triggerKey = new TriggerKey(jobInfo.getTriggerName(), jobInfo.getJobGroupName());
                if(scheduler.checkExists(triggerKey)) {
                    scheduler.unscheduleJob(triggerKey);
                    if(scheduler.checkExists(jobKey)) {
                        log.info("Trigger 가 제거되었습니다.");
                    } else {
                        log.info("Job 에 연관된 Trigger 가 모두 제거되어 Job 이 함께 제거되었습니다.");
                    }
                } else {
                    log.info("Trigger 가 존재하지 않습니다.");
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/update")
    public void update(@RequestBody UpdateJobInfo updateJobInfo) {
        try {
            Scheduler scheduler = quartzService.getSchedulerFactoryBean().getScheduler();

            JobInfo oldJobInfo = updateJobInfo.getOldJobInfo();
            JobInfo newJobInfo = updateJobInfo.getNewJobInfo();

            JobKey oldJobKey = new JobKey(oldJobInfo.getJobName(), oldJobInfo.getJobGroupName());
            JobKey newJobKey = new JobKey(newJobInfo.getJobName(), newJobInfo.getJobGroupName());

            TriggerKey oldTriggerKey = new TriggerKey(oldJobInfo.getTriggerName(), oldJobInfo.getJobGroupName());
            TriggerKey newTriggerKey = new TriggerKey(newJobInfo.getTriggerName(), newJobInfo.getJobGroupName());

            if (!scheduler.isStarted()) {
                log.info("스케줄러가 동작하고 있지 않습니다.");
                return;
            }
            if (!scheduler.getJobGroupNames().contains(oldJobInfo.getJobGroupName())) {
                log.info("JobGroup 이 존재하지 않습니다.");
                return;
            }
            if (!scheduler.checkExists(oldJobKey)) {
                log.info("JobName 이 존재하지 않습니다.");
                return;
            }
            if (!newJobKey.getGroup().equals(oldJobKey.getGroup()) && !newJobKey.getName().equals(oldJobKey.getName())) {
                if (scheduler.checkExists(newJobKey)) {
                    log.info("변경할 JobName 이 이미 존재합니다.");
                    return;
                }
            }
            if (!newTriggerKey.getGroup().equals(oldTriggerKey.getGroup()) && !newTriggerKey.getName().equals(oldTriggerKey.getName())) {
                if (scheduler.checkExists(newTriggerKey)) {
                    log.info("변경할 Trigger 가 이미 존재합니다.");
                    return;
                }
            }
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("cronExpression", newJobInfo.getCronExpression());
            jobDataMap.put("description", newJobInfo.getDescription());

            if("".equals(oldJobInfo.getTriggerName())) {
                List<Trigger> triggerList = (List<Trigger>) scheduler.getTriggersOfJob(oldJobKey);
                JobDetail newJobDetail = quartzService.jobDetail(newJobInfo.getClassName(), newJobInfo.getJobName(), newJobInfo.getJobGroupName());

                scheduler.addJob(newJobDetail, false, true);
                for(Trigger trigger : triggerList) {
                    Trigger triggerNew = quartzService.cronTrigger(trigger.getKey().getName(), trigger.getKey().getGroup(), trigger.getJobDataMap().get("cronExpression").toString(), trigger.getJobDataMap(), newJobDetail.getKey().getName());
                    scheduler.unscheduleJob(trigger.getKey());
                    scheduler.scheduleJob(triggerNew);
                }
                log.info("Job 과 연관된 Trigger 가 모두 수정되었습니다.");
            } else {
                if (!scheduler.checkExists(oldTriggerKey)) {
                    log.info("Trigger 가 존재하지 않습니다.");
                    return;
                }
                Trigger trigger = quartzService.cronTrigger(newJobInfo.getTriggerName(), newJobInfo.getJobGroupName(), newJobInfo.getCronExpression(), jobDataMap, newJobInfo.getJobName());

                scheduler.rescheduleJob(oldTriggerKey, trigger);
                log.info("Trigger 가 수정되었습니다.");
            }
        } catch (SchedulerException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
