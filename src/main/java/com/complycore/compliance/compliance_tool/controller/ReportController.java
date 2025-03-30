package com.complycore.compliance.compliance_tool.controller;

import com.complycore.compliance.compliance_tool.security.CustomUserDetails;
import com.complycore.compliance.compliance_tool.service.AuditLogService;
import com.complycore.compliance.compliance_tool.service.ReportService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.io.exceptions.IOException;
import com.opencsv.CSVWriter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final AuditLogService auditLogService;

    public ReportController(ReportService reportService, AuditLogService auditLogService) {
        this.reportService = reportService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<Map<String, Object>> getComplianceReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Map<String, Object> report = reportService.generateComplianceReport(startDate, endDate);
        auditLogService.log(principal.getUsername(), "REPORT_VIEWED",
                "Viewed compliance report" + (startDate != null ? " from " + startDate : "") + (endDate != null ? " to " + endDate : ""));

        return ResponseEntity.ok(report);
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<byte[]> exportReportAsCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal CustomUserDetails principal) throws java.io.IOException {

        Map<String, Object> report = reportService.generateComplianceReport(startDate, endDate);
        auditLogService.log(principal.getUsername(), "REPORT_EXPORTED_CSV", "Exported report as CSV");

        StringWriter stringWriter = new StringWriter();

        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            csvWriter.writeNext(new String[]{"ID", "Name", "Category", "Severity", "Status", "Last Checked"});

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailedRules = (List<Map<String, Object>>) report.get("detailedRules");

            if (detailedRules != null) {
                for (Map<String, Object> rule : detailedRules) {
                    csvWriter.writeNext(new String[]{
                            String.valueOf(rule.get("id")),
                            (String) rule.get("name"),
                            (String) rule.get("category"),
                            (String) rule.get("severity"),
                            (String) rule.get("status"),
                            rule.get("lastChecked") != null ? rule.get("lastChecked").toString() : "N/A"
                    });
                }
            }
        }

        byte[] csvBytes = stringWriter.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "compliance-report.csv");
        headers.setContentLength(csvBytes.length);

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<byte[]> exportReportAsPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal CustomUserDetails principal) {

        Map<String, Object> report = reportService.generateComplianceReport(startDate, endDate);
        auditLogService.log(principal.getUsername(), "REPORT_EXPORTED_PDF", "Exported report as PDF");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDocument = new PdfDocument(writer);
             Document document = new Document(pdfDocument)) {

            document.add(new Paragraph("Compliance Report"));
            document.add(new Paragraph("Generated on: " + LocalDateTime.now()));
            document.add(new Paragraph("Total Rules: " + report.get("totalRules")));
            document.add(new Paragraph("Total Checks: " + report.get("totalChecks")));
            document.add(new Paragraph(" "));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> detailedRules = (List<Map<String, Object>>) report.get("detailedRules");

            if (detailedRules != null) {
                for (Map<String, Object> rule : detailedRules) {
                    document.add(new Paragraph(
                            String.format("ID: %s, Name: %s, Category: %s, Severity: %s, Status: %s, Last Checked: %s",
                                    rule.get("id"), rule.get("name"), rule.get("category"), rule.get("severity"), rule.get("status"),
                                    rule.get("lastChecked") != null ? rule.get("lastChecked").toString() : "N/A")));
                }
            }
        } catch (IOException | java.io.IOException e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }

        byte[] pdfBytes = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "compliance-report.pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}