package org.example.mirimilibe.common.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.example.mirimilibe.common.domain.Specialty;
import org.example.mirimilibe.common.domain.Unit;
import org.example.mirimilibe.member.repository.UnitRepository;
import org.example.mirimilibe.post.repository.SpecialtyRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CSVLoadService {

	private final SpecialtyRepository specialtyRepository;
	private final UnitRepository unitRepository;

	@Transactional
	public void loadSpecialtyCSV() {
		if (specialtyRepository.count() > 0) {
			log.info("Specialty 데이터가 이미 존재합니다. 초기 로드하지 않습니다.");
			return;
		}

		try (Reader reader = new InputStreamReader(	new ClassPathResource("csv/specialty.csv").getInputStream(), StandardCharsets.UTF_8);
			 CSVReader csvReader = new CSVReader(reader)){
			List<Specialty> specialties = new ArrayList<>();
			String[] line;
			long num = 1;

			while ((line = csvReader.readNext()) != null) {
				Specialty specialty = Specialty.builder()
					.value(line[0])
					.specialtyId(num++)
					.build();
				specialties.add(specialty);
			}

			specialtyRepository.saveAll(specialties);
			log.info("Specialty 데이터 로드 완료: {}개 항목", specialties.size());

		} catch (IOException | CsvValidationException e) {
			log.warn("CSV 파일을 읽는 중 오류 발생: {}", e.getMessage());
		}
	}

	@Transactional
	public void loadUnitCSV(){
		if (unitRepository.count() > 0) {
			log.info("Unit 데이터가 이미 존재합니다. 초기 로드하지 않습니다.");
			return;
		}
		try (Reader reader = new InputStreamReader(new ClassPathResource("csv/unit.csv").getInputStream(), StandardCharsets.UTF_8);
			 CSVReader csvReader = new CSVReader(reader)){
			List<Unit> units = new ArrayList<>();
			String[] line;
			long num = 1;

			while ((line = csvReader.readNext()) != null) {
				Unit unit = Unit.builder()
					.value(line[0])
					.unitId(num++)
					.build();
				units.add(unit);
			}

			unitRepository.saveAll(units);
			log.info("Unit 데이터 로드 완료: {}개 항목", units.size());

		} catch (IOException | CsvValidationException e) {
			log.warn("CSV 파일을 읽는 중 오류 발생: {}", e.getMessage());
		}
	}

}
