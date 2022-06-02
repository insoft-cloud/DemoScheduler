package com.example.demo.quartzJob;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

@Slf4j
public abstract class BaseJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
            beforeExecution(context);
            doExecute(context);
            afterExecution(context);
            nextJobExecute(context);
    }

    protected abstract void doExecute(JobExecutionContext context);

    private void beforeExecution(JobExecutionContext context) {
        log.info("before Execution");
    }

    private void afterExecution(JobExecutionContext context) {
        log.info("after Execution");
    }

    private void nextJobExecute(JobExecutionContext context) {
        log.info("nextJob Execute");
    }
}
