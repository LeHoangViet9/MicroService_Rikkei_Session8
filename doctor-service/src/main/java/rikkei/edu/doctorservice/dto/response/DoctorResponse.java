package rikkei.edu.doctorservice.dto.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private String name;

    private String specialization;

    private Integer experienceYears;

    private String email;

    private Boolean status;
}
