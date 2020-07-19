package com.xdu.ichat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xdu.ichat.dao")
public class IchatApplication {

	public static void main(String[] args) {
		SpringApplication.run(IchatApplication.class, args);
	}

}
