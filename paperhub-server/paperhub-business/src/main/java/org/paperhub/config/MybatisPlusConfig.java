package org.paperhub.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.paperhub.auth.mapper")
public class MybatisPlusConfig {
}
