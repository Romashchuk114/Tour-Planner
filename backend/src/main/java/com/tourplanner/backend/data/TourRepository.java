package com.tourplanner.backend.data;

import com.tourplanner.backend.business.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Long> {
    List<Tour> findByUserIdOrderByCreatedAtDesc(Long userId);
}
