package rikkei.edu.doctorservice.service;

import rikkei.edu.doctorservice.dto.response.DoctorResponse;

import java.util.List;

public interface DoctorService {
    List<DoctorResponse> getAllDoctors();
}
