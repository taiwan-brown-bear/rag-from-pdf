package com.taiwan_brown_bear.rag_from_pdf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

//@CommandScan
@SpringBootApplication
public class RagFromPdfApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagFromPdfApplication.class, args);
	}

}
