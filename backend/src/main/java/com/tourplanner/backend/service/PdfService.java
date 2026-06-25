package com.tourplanner.backend.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.tourplanner.backend.model.Tour;
import com.tourplanner.backend.model.TourStage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    public byte[] createTourReport(Tour tour) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Tour Report: " + tour.getName()).setBold().setFontSize(20));
        document.add(new Paragraph("Beschreibung: " + tour.getDescription()));

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

        document.add(new Paragraph("\nZusammenfassung").setBold());
        double totalDistance = stages.stream().mapToDouble(TourStage::getDistance).sum();
        int totalDuration = stages.stream().mapToInt(TourStage::getDuration).sum();

        document.add(new Paragraph("Gesamtdistanz: " + String.format("%.2f", totalDistance) + " km"));
        document.add(new Paragraph("Geschätzte Gesamtdauer: " + formatDuration(totalDuration)));

        document.close();
        return baos.toByteArray();
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
