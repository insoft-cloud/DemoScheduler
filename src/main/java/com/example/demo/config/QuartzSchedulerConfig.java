package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.annotation.PostConstruct;

@Configuration
@RequiredArgsConstructor
public class QuartzSchedulerConfig {
    private final SchedulerFactoryBean schedulerFactoryBean;

    @PostConstruct
    public void scheduled() throws SchedulerException {
        schedulerFactoryBean.getScheduler().start();
    }
}
