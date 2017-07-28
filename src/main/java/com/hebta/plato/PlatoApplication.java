package com.hebta.plato;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ServletComponentScan
@MapperScan("com.hebta.plato.dao")
public class PlatoApplication {
	public static void main(String[] args) {
		SpringApplication.run(PlatoApplication.class, args);
	}
	
	@Bean
	public RestTemplate newRestTemplate(){
		return new RestTemplate();
	}
}
