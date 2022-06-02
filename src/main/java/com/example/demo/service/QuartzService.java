package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuartzService {
    private final SchedulerFactoryBean schedulerFactoryBean;

    public SchedulerFactoryBean getSchedulerFactoryBean() {
        return schedulerFactoryBean;
    }

    public JobDetail jobDetail(String className, String name, String group) throws ClassNotFoundException {
        JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) Class.forName("com.example.demo.quartzJob." + className))
                .withIdentity(name, group)
                .build();
        return jobDetail;
    }

    public CronTrigger cronTrigger(String name, String group, String cronExpression, JobDataMap jobDataMap, String jobName) {
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(name, group)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .usingJobData(jobDataMap)
                .forJob(jobName, group)
                .build();
        return cronTrigger;
    }
}
