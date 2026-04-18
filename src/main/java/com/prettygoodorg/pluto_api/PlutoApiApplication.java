package com.prettygoodorg.pluto_api;

import com.prettygoodorg.pluto_api.common.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(JwtProperties.class)
public class PlutoApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlutoApiApplication.class, args);
	}

}
