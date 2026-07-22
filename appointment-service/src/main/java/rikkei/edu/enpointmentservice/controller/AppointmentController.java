package rikkei.edu.enpointmentservice.controller;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rikkei.edu.enpointmentservice.dto.request.AppointmentRequest;
import rikkei.edu.enpointmentservice.dto.response.ApiResponseError;
import rikkei.edu.enpointmentservice.dto.response.AppointmentResponse;
import rikkei.edu.enpointmentservice.exception.ServiceUnavailableException;
import rikkei.edu.enpointmentservice.service.AppointmentService;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

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
                    .status(HttpStatus.SERVICE_UNAVAILABLE.value()) // HTTP 503
                    .error("Service Unavailable")
                    .message(e.getMessage())
                    .path(httpServletRequest.getRequestURI())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);

        } catch (IllegalArgumentException e) {
            ApiResponseError errorResponse = ApiResponseError.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value()) // HTTP 400
                    .error("Bad Request")
                    .message(e.getMessage())
                    .path(httpServletRequest.getRequestURI())
                    .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // ==========================================
    // BỔ SUNG CHO BÀI TẬP 4: RATE LIMITER
    // ==========================================
    @GetMapping("/doctors/search")
    @RateLimiter(name = "searchDoctorLimit", fallbackMethod = "searchDoctorRateLimitFallback")
    public ResponseEntity<?> searchDoctors(
            @RequestParam(required = false) String name,
            HttpServletRequest httpServletRequest) {

        // Trả về thông báo thành công cho request tìm kiếm
        return ResponseEntity.ok("Tìm kiếm bác sĩ thành công với từ khóa: " + (name != null ? name : "Tất cả"));
    }

    // Hàm Fallback được gọi tự động khi vượt quá 5 request / 10 giây
    public ResponseEntity<ApiResponseError> searchDoctorRateLimitFallback(
            String name,
            HttpServletRequest httpServletRequest,
            RequestNotPermitted ex) {

        ApiResponseError errorResponse = ApiResponseError.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value()) // HTTP 429
                .error("Too Many Requests")
                .message("Bạn đã vượt quá số lần tìm kiếm cho phép (tối đa 5 lần/10 giây). Vui lòng thử lại sau!")
                .path(httpServletRequest.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    @GetMapping("/check-insurance")
    public CompletableFuture<ResponseEntity<String>> checkInsurance(@RequestParam String insuranceId) {
        return appointmentService.checkInsuranceStatus(insuranceId)
                .thenApply(result -> ResponseEntity.ok(result));
    }
}