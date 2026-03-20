package com.tourplanner.backend.data;

import com.tourplanner.backend.business.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourLogRepository extends JpaRepository<TourLog, Long> {
    List<TourLog> findByTourIdOrderByDateTimeDesc(Long tourId);
}