package com.tourplanner.backend.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TourLog;
import com.tourplanner.backend.model.TourStage;
import com.tourplanner.backend.service.model.ComputedAttributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfService {

    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TourAttributeService tourAttributeService;
    private final ImageService imageService;

    public byte[] createTourReport(Tour tour) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Tour Report: " + tour.getName()).setBold().setFontSize(20));
        if (tour.getDescription() != null && !tour.getDescription().isBlank()) {
            document.add(new Paragraph("Beschreibung: " + tour.getDescription()));
        }

        addTourImage(document, tour);
        addRouteDetails(document, tour);
        addStageTable(document, tour);
        addSummary(document, tour);
        addLogTable(document, tour);

        document.close();
        return baos.toByteArray();
    }

    private void addTourImage(Document document, Tour tour) {
        if (tour.getTourImagePath() == null) {
            return;
        }
        try {
            byte[] data = imageService.load(tour.getTourImagePath()).getContentAsByteArray();
            Image image = new Image(ImageDataFactory.create(data));
            image.scaleToFit(300, 200);
            document.add(image);
        } catch (Exception e) {
            log.warn("Tour-Bild konnte nicht ins PDF übernommen werden: {}", tour.getTourImagePath());
        }
    }

    private void addRouteDetails(Document document, Tour tour) {
        document.add(new Paragraph("\nRouten Details").setBold());
        document.add(new Paragraph("Start: " + tour.getFromName()));

        List<TourStage> stages = tour.getStages();
        if (stages.size() > 1) {
            document.add(new Paragraph("Zwischenstopps:"));
            for (int i = 0; i < stages.size() - 1; i++) {
                document.add(new Paragraph("  - " + stages.get(i).getEndName()));
            }
        }
        if (!stages.isEmpty()) {
            document.add(new Paragraph("Ziel: " + stages.get(stages.size() - 1).getEndName()));
        }
    }

    private void addStageTable(Document document, Tour tour) {
        List<TourStage> stages = tour.getStages();
        if (stages.isEmpty()) {
            return;
        }

        document.add(new Paragraph("\nEtappen").setBold());
        Table table = new Table(UnitValue.createPercentArray(new float[]{6, 25, 25, 16, 14, 14}))
                .useAllAvailableWidth();
        addHeaderCells(table, "#", "Von", "Nach", "Transport", "Distanz", "Dauer");
        for (int i = 0; i < stages.size(); i++) {
            TourStage stage = stages.get(i);
            String from = i == 0 ? tour.getFromName() : stages.get(i - 1).getEndName();
            table.addCell(String.valueOf(i + 1));
            table.addCell(shortName(from));
            table.addCell(shortName(stage.getEndName()));
            table.addCell(stage.getTransportType().getLabel());
            table.addCell(String.format("%.2f km", stage.getDistance()));
            table.addCell(formatDuration(stage.getDuration()));
        }
        document.add(table);
    }

    private void addSummary(Document document, Tour tour) {
        List<TourStage> stages = tour.getStages();
        double totalDistance = stages.stream().mapToDouble(TourStage::getDistance).sum();
        int totalDuration = stages.stream().mapToInt(TourStage::getDuration).sum();
        ComputedAttributes attributes = tourAttributeService.computeForTour(tour.getId());

        document.add(new Paragraph("\nZusammenfassung").setBold());
        if (stages.size() > 1) {
            document.add(new Paragraph("Zwischenstopps: " + (stages.size() - 1)));
        }
        document.add(new Paragraph("Gesamtdistanz: " + String.format("%.2f", totalDistance) + " km"));
        document.add(new Paragraph("Geschätzte Gesamtdauer: " + formatDuration(totalDuration)));
        document.add(new Paragraph("Popularität: " + attributes.popularity() + " (" + attributes.logCount() + " Logs)"));
        document.add(new Paragraph("Kinderfreundlichkeit: " + attributes.childFriendliness()));
    }

    private void addLogTable(Document document, Tour tour) {
        List<TourLog> logs = tour.getLogs().stream()
                .sorted(Comparator.comparing(TourLog::getDateTime).reversed())
                .toList();
        if (logs.isEmpty()) {
            return;
        }

        document.add(new Paragraph("\nTour Logs").setBold());
        Table table = new Table(UnitValue.createPercentArray(new float[]{17, 12, 12, 14, 11, 34}))
                .useAllAvailableWidth();
        addHeaderCells(table, "Datum", "Distanz", "Dauer", "Schwierigkeit", "Bewertung", "Kommentar");
        for (TourLog log : logs) {
            table.addCell(log.getDateTime().format(LOG_DATE_FORMAT));
            table.addCell(log.getTotalDistance() + " km");
            table.addCell(formatDuration(log.getTotalTime()));
            table.addCell(log.getDifficulty() + "/10");
            table.addCell(log.getRating() + "/5");
            table.addCell(log.getComment() == null ? "-" : log.getComment());
        }
        document.add(table);
    }

    private void addHeaderCells(Table table, String... headers) {
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header).setBold()));
        }
    }

    private String shortName(String location) {
        return location.split(",")[0].trim();
    }

    private String formatDuration(int totalMinutes) {
        if (totalMinutes < 0) return "N/A";
        if (totalMinutes == 0) return "0 min";

        if (totalMinutes < 60) {
            return totalMinutes + " min";
        }

        long hours = totalMinutes / 60;
        long remainingMinutes = totalMinutes % 60;

        if (hours < 24) {
            if (remainingMinutes == 0) return hours + " h";
            return hours + " h / " + remainingMinutes + " min";
        }

        long days = hours / 24;
        long remainingHours = hours % 24;

        StringBuilder result = new StringBuilder();
        result.append(days).append(" d / ");
        if (remainingHours > 0) {
            result.append(" ").append(remainingHours).append(" h");
        }
        if (remainingMinutes > 0) {
            result.append(" ").append(remainingMinutes).append(" min");
        }

        return result.toString();
    }
}
