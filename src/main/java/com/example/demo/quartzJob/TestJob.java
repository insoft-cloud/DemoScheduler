package com.example.demo.quartzJob;

import com.example.demo.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import java.time.LocalDateTime;

@Slf4j
public class TestJob extends BaseJob {
    private final Job batchJob;
    private final JobLauncher jobLauncher;

    public TestJob() {
        batchJob = BeanUtils.getBean(Job.class);
        jobLauncher = BeanUtils.getBean(JobLauncher.class);
    }

    @Override
    protected void doExecute(JobExecutionContext context) {
        try {
            jobLauncher.run(batchJob, new JobParametersBuilder().addString("datetime", LocalDateTime.now().toString()).toJobParameters());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }
}
