package com.tourplanner.backend.data;

import com.tourplanner.backend.model.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TourLogRepository extends JpaRepository<TourLog, Long> {
    List<TourLog> findByTourIdOrderByDateTimeDesc(Long tourId);

    List<TourLog> findByTourUserId(Long userId);

    @Query("select l.tour.id as tourId, count(l) as logCount, avg(l.difficulty) as avgDifficulty, " +
           "avg(l.totalTime) as avgTime, avg(l.totalDistance) as avgDistance " +
           "from TourLog l where l.tour.user.id = :userId group by l.tour.id")
    List<TourLogAggregate> findAggregatesByUserId(@Param("userId") Long userId);

    @Query("select l.tour.id as tourId, count(l) as logCount, avg(l.difficulty) as avgDifficulty, " +
           "avg(l.totalTime) as avgTime, avg(l.totalDistance) as avgDistance " +
           "from TourLog l where l.tour.id = :tourId group by l.tour.id")
    Optional<TourLogAggregate> findAggregateByTourId(@Param("tourId") Long tourId);
}
