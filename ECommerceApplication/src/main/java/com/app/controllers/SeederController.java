package com.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.services.SeederService;

@RestController
@RequestMapping("/api")
public class SeederController {

	@Autowired
	private SeederService seederService;

	@PostMapping("/public/seed")
	public ResponseEntity<String> seed() {
		String result = seederService.seedAll();
		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}
}
