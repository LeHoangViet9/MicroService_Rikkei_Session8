package rikkei.edu.patientservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rikkei.edu.patientservice.dto.request.PatientRequest;
import rikkei.edu.patientservice.dto.response.PatientResponse;
import rikkei.edu.patientservice.service.PatientService;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@RequestBody PatientRequest patientRequest) {
        PatientResponse response = patientService.createPatient(patientRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
