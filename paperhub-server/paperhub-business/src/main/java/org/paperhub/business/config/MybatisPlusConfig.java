package org.paperhub.business.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("org.paperhub.business.mapper")
public class MybatisPlusConfig {
}
