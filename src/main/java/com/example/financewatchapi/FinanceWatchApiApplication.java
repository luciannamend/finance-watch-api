package com.example.financewatchapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class FinanceWatchApiApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(FinanceWatchApiApplication.class, args);

		System.out.println("\n============================================================");
		System.out.println("             Finance Watch Api Started");
		System.out.println("============================================================\n");
	}
}
