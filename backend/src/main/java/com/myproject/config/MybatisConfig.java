package com.myproject.config;


import com.baomidou.mybatisplus.extension.MybatisMapWrapperFactory;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MybatisConfig {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @PostConstruct
    public void setObjectWrapperFactory() {
        // 在项目启动后设置ObjectWrapperFactory
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().setObjectWrapperFactory(new MybatisMapWrapperFactory());
        }
    }
}