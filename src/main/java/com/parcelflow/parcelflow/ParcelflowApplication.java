package com.parcelflow.parcelflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
public class ParcelflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParcelflowApplication.class, args);
	}

}
