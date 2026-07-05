package com.tourplanner.backend.service;

import com.tourplanner.backend.data.TourLogRepository;
import com.tourplanner.backend.data.TourRepository;
import com.tourplanner.backend.data.UserRepository;
import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TourLog;
import com.tourplanner.backend.model.TourStage;
import com.tourplanner.backend.model.TransportType;
import com.tourplanner.backend.model.User;
import com.tourplanner.backend.service.exception.ForbiddenException;
import com.tourplanner.backend.service.exception.ResourceNotFoundException;
import com.tourplanner.backend.service.model.ComputedAttributes;
import com.tourplanner.backend.service.model.RouteInfo;
import com.tourplanner.backend.service.model.StageParam;
import com.tourplanner.backend.service.model.TourRequestParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock private TourRepository tourRepository;
    @Mock private TourLogRepository tourLogRepository;
    @Mock private UserRepository userRepository;
    @Mock private ImageService imageService;
    @Mock private RouteService routeService;
    @Mock private PdfService pdfService;
    @Mock private WeatherService weatherService;

    @InjectMocks
    private TourService tourService;

    private User user(long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("testuser");
        return user;
    }

    private Tour tour(long id, long userId) {
        Tour tour = new Tour();
        tour.setId(id);
        tour.setUser(user(userId));
        tour.setName("Testtour");
        tour.setFromName("Wien");
        tour.setFromLat(48.2);
        tour.setFromLng(16.37);
        return tour;
    }

    @Test
    void createBuildsStagesWithRouteDataFromOrs() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(routeService.fetchRoute(48.2, 16.37, 48.3, 14.28, TransportType.BIKE))
                .thenReturn(new RouteInfo(180.5, 720, "geo1"));
        when(routeService.fetchRoute(48.3, 14.28, 47.8, 13.04, TransportType.CAR))
                .thenReturn(new RouteInfo(130.0, 90, "geo2"));
        when(tourRepository.save(any(Tour.class))).thenAnswer(inv -> inv.getArgument(0));

        TourRequestParams params = new TourRequestParams("Testtour", null, "Wien", 48.2, 16.37, List.of(
                new StageParam(TransportType.BIKE, "Linz", 48.3, 14.28),
                new StageParam(TransportType.CAR, "Salzburg", 47.8, 13.04)
        ));

        Tour created = tourService.create(1L, params);

        assertEquals(2, created.getStages().size());
        TourStage first = created.getStages().get(0);
        assertEquals(0, first.getOrderIndex());
        assertEquals(180.5, first.getDistance());
        assertEquals(720, first.getDuration());
        assertEquals("geo1", first.getGeometryGeoJson());
        TourStage second = created.getStages().get(1);
        assertEquals(1, second.getOrderIndex());
        assertEquals("Salzburg", second.getEndName());
        assertEquals("geo2", second.getGeometryGeoJson());
    }

    @Test
    void getByIdThrowsForbiddenForForeignTour() {
        when(tourRepository.findById(5L)).thenReturn(Optional.of(tour(5L, 1L)));

        assertThrows(ForbiddenException.class, () -> tourService.getById(5L, 99L));
    }

    @Test
    void getByIdThrowsNotFoundForUnknownTour() {
        when(tourRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tourService.getById(5L, 1L));
    }

    @Test
    void deleteRemovesTourAndImage() {
        Tour tour = tour(5L, 1L);
        tour.setTourImagePath("tour_5.png");
        when(tourRepository.findById(5L)).thenReturn(Optional.of(tour));

        tourService.delete(5L, 1L);

        verify(imageService).delete("tour_5.png");
        verify(tourRepository).delete(tour);
    }

    @Test
    void searchWithBlankQueryReturnsAllTours() {
        List<Tour> tours = List.of(tour(1L, 1L), tour(2L, 1L));
        when(tourRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(tours);

        assertEquals(2, tourService.search(1L, "  ", Map.of()).size());
        assertEquals(2, tourService.search(1L, null, Map.of()).size());
    }

    @Test
    void searchMatchesTourNameCaseInsensitive() {
        Tour tour = tour(1L, 1L);
        tour.setName("Donau-Etappe");
        when(tourRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTourUserId(1L)).thenReturn(List.of());

        assertEquals(1, tourService.search(1L, "dOnAu", Map.of()).size());
    }

    @Test
    void searchMatchesLogComment() {
        Tour tour = tour(1L, 1L);
        TourLog log = new TourLog();
        log.setTour(tour);
        log.setComment("Der Burggarten war toll");
        when(tourRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTourUserId(1L)).thenReturn(List.of(log));

        assertEquals(1, tourService.search(1L, "burggarten", Map.of()).size());
    }

    @Test
    void searchMatchesComputedAttributeLabel() {
        Tour tour = tour(1L, 1L);
        when(tourRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTourUserId(1L)).thenReturn(List.of());
        Map<Long, ComputedAttributes> attributes =
                Map.of(1L, new ComputedAttributes(12, "Sehr hoch", "Gut geeignet"));

        assertEquals(1, tourService.search(1L, "sehr hoch", attributes).size());
        assertEquals(1, tourService.search(1L, "geeignet", attributes).size());
    }

    @Test
    void searchMatchesTransportTypeLabel() {
        Tour tour = tour(1L, 1L);
        TourStage stage = new TourStage();
        stage.setEndName("Linz");
        stage.setTransportType(TransportType.BIKE);
        tour.getStages().add(stage);
        when(tourRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(tour));
        when(tourLogRepository.findByTourUserId(1L)).thenReturn(List.of());

        assertEquals(1, tourService.search(1L, "Fahrrad", Map.of()).size());
    }

    @Test
    void searchReturnsEmptyListWithoutMatch() {
        when(tourRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(tour(1L, 1L)));
        when(tourLogRepository.findByTourUserId(1L)).thenReturn(List.of());

        assertTrue(tourService.search(1L, "xyz123", Map.of()).isEmpty());
    }
}
