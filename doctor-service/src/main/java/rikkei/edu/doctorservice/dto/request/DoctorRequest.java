package rikkei.edu.doctorservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRequest {
    private String name;

    private String specialization;

    private Integer experienceYears;

    private String email;
}
