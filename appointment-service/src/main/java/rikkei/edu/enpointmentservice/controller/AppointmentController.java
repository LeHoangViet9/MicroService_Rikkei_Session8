package rikkei.edu.enpointmentservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rikkei.edu.enpointmentservice.dto.request.AppointmentRequest;
import rikkei.edu.enpointmentservice.dto.response.ApiResponseError;
import rikkei.edu.enpointmentservice.dto.response.AppointmentResponse;
import rikkei.edu.enpointmentservice.entity.Appointment;
import rikkei.edu.enpointmentservice.exception.ServiceUnavailableException;
import rikkei.edu.enpointmentservice.service.AppointmentService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<?> createAppointment(
            @RequestBody AppointmentRequest request,
            HttpServletRequest httpServletRequest) {
        try {
            AppointmentResponse appointment = appointmentService.createAppointment(request);
            return new ResponseEntity<>(appointment, HttpStatus.CREATED);

        } catch (ServiceUnavailableException e) {
            ApiResponseError errorResponse = ApiResponseError.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                    .error("Service Unavailable")
                    .message(e.getMessage())
                    .path(httpServletRequest.getRequestURI())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);

        } catch (IllegalArgumentException e) {
            ApiResponseError errorResponse = ApiResponseError.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Bad Request")
                    .message(e.getMessage())
                    .path(httpServletRequest.getRequestURI())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
