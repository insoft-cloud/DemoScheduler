package com.example.demo.utils;

import org.springframework.context.ApplicationContext;

public class BeanUtils {
    public static <T> T getBean(Class<T> classType) {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        return applicationContext.getBean(classType);
    }
}
