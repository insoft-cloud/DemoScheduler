package com.example.demo.vo;

import lombok.Data;

@Data
public class JobInfo {
    private String className;
    private String jobName;
    private String jobGroupName;
    private String triggerName;
    private String cronExpression;
    private String description;

    public JobInfo() {
        className = "";
        jobName = "";
        jobGroupName = "";
        triggerName = "";
        cronExpression = "";
        description = "";
    }
}
