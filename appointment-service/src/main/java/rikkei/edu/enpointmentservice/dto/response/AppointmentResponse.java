package rikkei.edu.enpointmentservice.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {
    private Long id;

    private Long patientId;

    private Long doctorId;

    private LocalDateTime appointmentDate;

    private String reason;

    private String status;
}
