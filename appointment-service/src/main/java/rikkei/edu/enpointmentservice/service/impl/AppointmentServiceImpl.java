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

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointRepository appointRepository;
    private final RestTemplate restTemplate;

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // 1. Kiểm tra Patient-Service
        checkPatientExists(request.getPatientId());

        // 2. Kiểm tra Doctor-Service (Đã áp dụng Circuit Breaker)
        checkDoctorExists(request.getDoctorId());

        // 3. Tiến hành tạo lịch hẹn
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

    // --- LOGIC GỌI DOCTOR-SERVICE CÓ CIRCUIT BREAKER ---

    @CircuitBreaker(name = "doctorServiceCB", fallbackMethod = "doctorFallback")
    public void checkDoctorExists(Long doctorId) {
        String doctorUrl = "http://doctor-service/api/v1/doctors/" + doctorId;
        try {
            restTemplate.getForEntity(doctorUrl, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            // Lỗi 404 là do dữ liệu truyền vào sai (business error), KHÔNG ném lỗi để CB đếm thất bại
            throw new IllegalArgumentException("Lỗi: Bác sĩ có ID " + doctorId + " không tồn tại!");
        }
        // Nếu Doctor-Service sập (ResourceAccessException / RestClientException),
        // exception sẽ bay ra ngoài để @CircuitBreaker ghi nhận 1 lần thất bại.
    }

    // Hàm Fallback khi Doctor-Service sập HOẶC khi mạch đang OPEN
    public void doctorFallback(Long doctorId, Throwable throwable) {
        log.error("Doctor-Service fallback triggered for doctorId: {}. Reason: {}", doctorId, throwable.getMessage());

        // Nếu là lỗi dữ liệu truyền vào (404 Not Found), chuyển tiếp lỗi
        if (throwable instanceof IllegalArgumentException) {
            throw (IllegalArgumentException) throwable;
        }

        // Với các trường hợp khác (Server sập, Timeout, Mạch đang OPEN), ném lỗi ServiceUnavailable
        throw new ServiceUnavailableException("Hệ thống kiểm tra Bác sĩ hiện không khả dụng. Vui lòng thử lại sau!");
    }

    // --- LOGIC GỌI PATIENT-SERVICE ---

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
}