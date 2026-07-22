package rikkei.edu.enpointmentservice.service;

import rikkei.edu.enpointmentservice.dto.request.AppointmentRequest;
import rikkei.edu.enpointmentservice.dto.response.AppointmentResponse;

import java.util.concurrent.CompletableFuture;

public interface AppointmentService {
    AppointmentResponse createAppointment(AppointmentRequest request);
    CompletableFuture<String> checkInsuranceStatus(String patientInsuranceId);
    CompletableFuture<String> checkInsuranceFallback(String patientInsuranceId, Throwable t);
    void checkDoctorExistsWithResilience(Long doctorId);
    void doctorResilienceFallback(Long doctorId, Throwable throwable);
}
