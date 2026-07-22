package rikkei.edu.patientservice.service;

import rikkei.edu.patientservice.dto.request.PatientRequest;
import rikkei.edu.patientservice.dto.response.PatientResponse;

public interface PatientService {
    PatientResponse createPatient(PatientRequest patientRequest);
}
