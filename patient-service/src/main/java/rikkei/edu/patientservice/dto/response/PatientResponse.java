package rikkei.edu.patientservice.dto.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class PatientResponse {
    private Long id;

    private String fullName;

    private LocalDate dateOfBirth;

    private String gender;

    private String phoneNumber;

    private String address;

    private String medicalHistory;
}
