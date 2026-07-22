package rikkei.edu.doctorservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rikkei.edu.doctorservice.dto.response.DoctorResponse;
import rikkei.edu.doctorservice.service.DoctorService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        List<DoctorResponse> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }
}
