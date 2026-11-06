package com.example.daon.config;

import java.util.Properties;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 접속한 DB 종류를 MyBatis에 알려주는 설정.
 * product.xml 에서 databaseId="mysql" / databaseId="postgresql" 로
 * 같은 쿼리를 DB별로 나눠 쓸 수 있게 해준다.
 * (이클립스 로컬 = MySQL, 배포 = Supabase PostgreSQL)
 */
@Configuration
public class DatabaseIdConfig {

    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
        Properties props = new Properties();
        props.setProperty("MySQL", "mysql");
        props.setProperty("PostgreSQL", "postgresql");
        provider.setProperties(props);
        return provider;
    }
}
