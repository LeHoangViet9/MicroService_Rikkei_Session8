package rikkei.edu.doctorservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikkei.edu.doctorservice.entity.Doctor;
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
