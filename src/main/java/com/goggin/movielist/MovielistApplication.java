package com.goggin.movielist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class MovielistApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovielistApplication.class, args);
		log.info("App started running!");
	}

}
