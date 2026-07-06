package com.tourplanner.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tour_stages", uniqueConstraints =
        @UniqueConstraint(columnNames = {"tour_id", "order_index"}))
@Getter
@Setter
@NoArgsConstructor
public class TourStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false)
    private TransportType transportType;

    @Column(name = "end_name", nullable = false)
    private String endName;

    @Column(name = "end_lat", nullable = false)
    private Double endLat;

    @Column(name = "end_lng", nullable = false)
    private Double endLng;

    @Column(nullable = false)
    private Double distance;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "geometry_geojson", columnDefinition = "TEXT")
    private String geometryGeoJson;
}
