package rikkei.edu.enpointmentservice.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import rikkei.edu.enpointmentservice.dto.request.AppointmentRequest;
import rikkei.edu.enpointmentservice.dto.response.AppointmentResponse;
import rikkei.edu.enpointmentservice.entity.Appointment;
import rikkei.edu.enpointmentservice.exception.ServiceUnavailableException;
import rikkei.edu.enpointmentservice.repository.AppointRepository;
import rikkei.edu.enpointmentservice.service.AppointmentService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointRepository appointRepository;
    private final RestTemplate restTemplate;

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // 1. Kiem tra Patient Service
        checkPatientExists(request.getPatientId());

        // 2. Kiem tra Doctor Service (Goi qua method duoc boc @CircuitBreaker)
        checkDoctorExists(request.getDoctorId());

        // 3. Luu thong tin cuoc hen
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setAppointmentDate(LocalDateTime.now().plusDays(1));
        appointment.setReason("Khám sức khỏe tổng quát");
        appointment.setStatus("PENDING");

        Appointment savedAppointment = appointRepository.save(appointment);

        return AppointmentResponse.builder()
                .id(savedAppointment.getId())
                .status(savedAppointment.getStatus())
                .doctorId(savedAppointment.getDoctorId())
                .appointmentDate(savedAppointment.getAppointmentDate())
                .patientId(savedAppointment.getPatientId())
                .reason(savedAppointment.getReason())
                .build();
    }

    private void checkPatientExists(Long patientId) {
        String patientUrl = "http://patient-service/api/v1/patients/" + patientId;
        try {
            restTemplate.getForEntity(patientUrl, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Lỗi: Bệnh nhân có ID " + patientId + " không tồn tại!");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Không thể kết nối tới Patient-Service!");
        }
    }

    @CircuitBreaker(name = "doctorServiceCB", fallbackMethod = "getDoctorFallback")
    public void checkDoctorExists(Long doctorId) {
        String doctorUrl = "http://doctor-service/api/v1/doctors/" + doctorId;
        try {
            restTemplate.getForEntity(doctorUrl, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Lỗi: Bác sĩ có ID " + doctorId + " không tồn tại!");
        }
    }

    // Fallback Method dung theo yeu cau bai tap
    public void getDoctorFallback(Long doctorId, Exception e) {
        log.error("Circuit Breaker kich hoat cho Doctor-Service do loi: {}", e.getMessage());

        // Neu bac si khong ton tai (404), van throw IllegalArgumentException cho controller xu ly 400
        if (e instanceof IllegalArgumentException) {
            throw (IllegalArgumentException) e;
        }

        // Truong hop Doctor-Service sap hoac Circuit Breaker OPEN -> Nem ra loi 503
        throw new ServiceUnavailableException("Hiện tại không thể kiểm tra thông tin bác sĩ, vui lòng thử lại sau vài giây");
    }
}