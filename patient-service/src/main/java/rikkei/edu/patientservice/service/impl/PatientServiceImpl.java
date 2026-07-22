package rikkei.edu.patientservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rikkei.edu.patientservice.dto.request.PatientRequest;
import rikkei.edu.patientservice.dto.response.PatientResponse;
import rikkei.edu.patientservice.entity.Patients;
import rikkei.edu.patientservice.repository.PatientRepository;
import rikkei.edu.patientservice.service.PatientService;
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    @Override
    public PatientResponse createPatient(PatientRequest patientRequest) {
        Patients patient = new Patients();
        patient.setFullName(patientRequest.getFullName());
        patient.setAddress(patientRequest.getAddress());
        patient.setDateOfBirth(patientRequest.getDateOfBirth());
        patient.setGender(patientRequest.getGender());
        patient.setPhoneNumber(patientRequest.getPhoneNumber());
        Patients savedPatient= patientRepository.save(patient);
        return PatientResponse.builder()
                .id(savedPatient.getId())
                .fullName(savedPatient.getFullName())
                .address(savedPatient.getAddress())
                .dateOfBirth(savedPatient.getDateOfBirth())
                .gender(savedPatient.getGender())
                .phoneNumber(savedPatient.getPhoneNumber())
                .medicalHistory(savedPatient.getMedicalHistory())
                .build();
    }
}
