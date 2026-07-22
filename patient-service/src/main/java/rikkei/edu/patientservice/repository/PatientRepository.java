package rikkei.edu.patientservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikkei.edu.patientservice.entity.Patients;
@Repository
public interface PatientRepository extends JpaRepository<Patients,Long> {
}
