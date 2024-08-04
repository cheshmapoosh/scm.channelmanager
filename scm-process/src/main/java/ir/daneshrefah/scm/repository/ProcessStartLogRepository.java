package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.ProcessStartLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessStartLogRepository extends JpaRepository<ProcessStartLog,Long> {
}
