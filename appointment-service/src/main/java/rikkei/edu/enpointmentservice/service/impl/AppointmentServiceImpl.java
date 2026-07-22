package rikkei.edu.enpointmentservice.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
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
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointRepository appointRepository;
    private final RestTemplate restTemplate;

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        // 1. Kiem tra Patient Service (Co Retry + Fallback)
        checkPatientExists(request.getPatientId());

        // 2. Kiem tra Doctor Service (Circuit Breaker)
        checkDoctorExists(request.getDoctorId());

        // 3. Luu cuoc hen
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

    // Ten 'patientRetry' phai trung khop voi application.properties
    @Retry(name = "patientRetry", fallbackMethod = "patientFallback")
    public void checkPatientExists(Long patientId) {
        log.info("Dang goi sang Patient-Service de kiem tra patientId = {}", patientId);
        String patientUrl = "http://patient-service/api/v1/patients/" + patientId;

        try {
            restTemplate.getForEntity(patientUrl, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            // Neu benh nhan 404 nguyen nhan ro rang, khong can retry -> throw truc tiep
            throw new IllegalArgumentException("Lỗi: Bệnh nhân có ID " + patientId + " không tồn tại!");
        }
    }

    // Fallback Method: Chi chay khi da Retry du 3 lan ma van loi
    public void patientFallback(Long patientId, Exception e) {
        log.error("Da retry 3 lan nhung Patient-Service van khong phan hoi: {}", e.getMessage());

        if (e instanceof IllegalArgumentException) {
            throw (IllegalArgumentException) e;
        }

        throw new ServiceUnavailableException("Không thể kết nối tới Patient-Service sau nhiều lần thử lại. Vui lòng thử lại sau!");
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

    public void getDoctorFallback(Long doctorId, Exception e) {
        if (e instanceof IllegalArgumentException) {
            throw (IllegalArgumentException) e;
        }
        throw new ServiceUnavailableException("Hiện tại không thể kiểm tra thông tin bác sĩ, vui lòng thử lại sau vài giây");
    }


    @TimeLimiter(name = "insuranceTimeout", fallbackMethod = "checkInsuranceFallback")
    public CompletableFuture<String> checkInsuranceStatus(String patientInsuranceId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Đang kết nối tới Insurance-Service để kiểm tra BHYT...");

            try {
                // Giả lập Insurance-Service xử lý rất chậm (mất 3 giây)
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return "BHYT Hợp lệ - Được giảm 80% chi phí";
        });
    }

    /**
     * Fallback Method xử lý khi quá 1s (TimeoutException)
     * Giúp hệ thống không bị treo, tự động chuyển sang thanh toán trực tiếp
     */
    public CompletableFuture<String> checkInsuranceFallback(String patientInsuranceId, Throwable t) {
        log.warn("Insurance-Service phản hồi quá lâu (>1s) hoặc gặp sự cố: {}", t.getMessage());

        // Trả về kết quả hoàn thành ngay lập tức
        return CompletableFuture.completedFuture(
                "Insurance-Service phản hồi quá lâu. Đã bỏ qua bước kiểm tra BHYT, cho phép thanh toán trực tiếp!"
        );
    }

    /**
     * THỨ TỰ THỰC THI (TỪ NGOÀI VÀO TRONG):
     * 1. @RateLimiter  -> Chống spam/quá tải (Nêu vượt quá -> nhảy vào Fallback ngay)
     * 2. @CircuitBreaker -> Ngắt mạch nếu tỉ lệ lỗi vượt 50%
     * 3. @Retry         -> Thử lại 3 lần nếu gặp lỗi kết nối/mạng tạm thời
     */
    @RateLimiter(name = "doctorServiceLimiter", fallbackMethod = "doctorResilienceFallback")
    @CircuitBreaker(name = "doctorServiceCB", fallbackMethod = "doctorResilienceFallback")
    @Retry(name = "doctorServiceRetry", fallbackMethod = "doctorResilienceFallback")
    public void checkDoctorExistsWithResilience(Long doctorId) {
        log.info("Gửi request kết nối tới Doctor-Service cho Doctor ID = {}", doctorId);
        String doctorUrl = "http://doctor-service/api/v1/doctors/" + doctorId;

        try {
            restTemplate.getForEntity(doctorUrl, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            // Neu bac si khong ton tai (404), throw IllegalArgumentException de khong tinh vao loi Retry
            throw new IllegalArgumentException("Lỗi: Bác sĩ có ID " + doctorId + " không tồn tại!");
        }
    }

    /**
     * Fallback chung duy nhất xử lý cho cả 3 tầng (RateLimiter, CircuitBreaker, Retry)
     */
    public void doctorResilienceFallback(Long doctorId, Throwable throwable) {
        log.error("Hệ thống Kích hoạt Fallback do lỗi: {}", throwable.getMessage());

        // Nếu là lỗi validation thông thường -> Bắn ra 400 Bad Request
        if (throwable instanceof IllegalArgumentException) {
            throw (IllegalArgumentException) throwable;
        }

        // Nếu bị Rate Limiter chặn -> Bắn ra lỗi 429
        if (throwable instanceof RequestNotPermitted) {
            throw new ServiceUnavailableException("Bạn đã vượt quá số lần thao tác cho phép (Rate Limit). Cuộc hẹn đã được ghi nhận ở trạng thái PENDING!");
        }

        // Nếu bị ngắt mạch (Circuit Breaker OPEN) hoặc Retry thất bại 3 lần
        throw new ServiceUnavailableException("Hệ thống kiểm tra bác sĩ hiện không khả dụng. Cuộc hẹn của bạn đã được lưu ở trạng thái PENDING!");
    }
}