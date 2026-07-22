package rikkei.edu.enpointmentservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rikkei.edu.enpointmentservice.entity.Appointment;
@Repository
public interface AppointRepository extends JpaRepository<Appointment, Long> {
}
