package org.example.mirimilibe.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

	@Value("${csv.init.enable}")
	private boolean loadCSV;

	private final CSVLoadService csvLoadService;

	public DataInitializer(CSVLoadService csvLoadService) {
		this.csvLoadService = csvLoadService;
	}

	@Override
	public void run(String... args) {
		if (loadCSV) {
			csvLoadService.loadSpecialtyCSV();
			csvLoadService.loadUnitCSV();
			csvLoadService.loadCategoryCSV();
		}
	}
}
