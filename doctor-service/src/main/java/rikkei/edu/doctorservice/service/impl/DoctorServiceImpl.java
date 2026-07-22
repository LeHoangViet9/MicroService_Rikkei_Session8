package rikkei.edu.doctorservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rikkei.edu.doctorservice.dto.response.DoctorResponse;
import rikkei.edu.doctorservice.entity.Doctor;
import rikkei.edu.doctorservice.repository.DoctorRepository;
import rikkei.edu.doctorservice.service.DoctorService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;

    @Override
    public List<DoctorResponse> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();

        return doctors.stream()
                .map(doctor -> DoctorResponse.builder()
                        .id(doctor.getId())
                        .name(doctor.getName())
                        .specialization(doctor.getSpecialization())
                        .build())
                .collect(Collectors.toList());
    }
}
