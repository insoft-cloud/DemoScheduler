package com.example.demo.vo;

import lombok.Data;

@Data
public class UpdateJobInfo {
    private JobInfo oldJobInfo;
    private JobInfo newJobInfo;
}
