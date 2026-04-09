package com.tourplanner.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_logs")
@Getter
@Setter
@NoArgsConstructor
public class TourLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    private String comment;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(name = "total_distance", nullable = false)
    private Double totalDistance;

    @Column(name = "total_time", nullable = false)
    private Integer totalTime;

    @Column(nullable = false)
    private Integer rating;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}