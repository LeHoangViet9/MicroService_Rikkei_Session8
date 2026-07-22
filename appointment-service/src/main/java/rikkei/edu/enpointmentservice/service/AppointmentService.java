package rikkei.edu.enpointmentservice.service;

import rikkei.edu.enpointmentservice.dto.request.AppointmentRequest;
import rikkei.edu.enpointmentservice.dto.response.AppointmentResponse;

public interface AppointmentService {
    AppointmentResponse createAppointment(AppointmentRequest request);
}
