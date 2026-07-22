package rikkei.edu.enpointmentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import rikkei.edu.enpointmentservice.config.WebConfig;
import rikkei.edu.enpointmentservice.dto.request.AppointmentRequest;
import rikkei.edu.enpointmentservice.dto.response.AppointmentResponse;
import rikkei.edu.enpointmentservice.entity.Appointment;
import rikkei.edu.enpointmentservice.exception.ServiceUnavailableException;
import rikkei.edu.enpointmentservice.repository.AppointRepository;
import rikkei.edu.enpointmentservice.service.AppointmentService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointRepository appointRepository;
    private final RestTemplate restTemplate;
    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        String patientUrl="http://patient-service/api/v1/patients/" + request.getPatientId();
        try {
            restTemplate.getForEntity(patientUrl, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Lỗi: Bệnh nhân có ID " + request.getPatientId() + " không tồn tại!");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Không thể kết nối tới Patient-Service!");
        }

        String doctorUrl="http://doctor-service/api/v1/doctors/" + request.getDoctorId();
        try{
            restTemplate.getForEntity(doctorUrl, Object.class);
        }catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Lỗi: Bác sĩ có ID " + request.getPatientId() + " không tồn tại!");
        } catch (Exception e) {
            throw new ServiceUnavailableException("Không thể kết nối tới Patient-Service!");
        }
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());

        appointment.setAppointmentDate(LocalDateTime.now().plusDays(1));
        appointment.setReason("Khám sức khỏe tổng quát");
        appointment.setStatus("PENDING");

        Appointment savedAppointment1= appointRepository.save(appointment);
        return AppointmentResponse.builder()
                .id(savedAppointment1.getId())
                .status(savedAppointment1.getStatus())
                .doctorId(savedAppointment1.getDoctorId())
                .appointmentDate(savedAppointment1.getAppointmentDate())
                .patientId(savedAppointment1.getPatientId())
                .reason(savedAppointment1.getReason())
                .build();
    }
}
