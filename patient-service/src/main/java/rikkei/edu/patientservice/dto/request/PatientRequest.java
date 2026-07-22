package rikkei.edu.patientservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class PatientRequest {
    private String fullName;
    private String address;
    private LocalDate dateOfBirth;
    private String gender;
    private String phoneNumber;
    private String medicalHistory;
}
