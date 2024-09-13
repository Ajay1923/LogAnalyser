package com.crud.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private List<String> allLogs;
    private List<String> detailedErrorLogs;

    @GetMapping("/")
    public String index() {
        return "webpage";
    }

    @GetMapping("/webpage")
    public String webpage(Model model) {
        return "webpage";
    }

    @PostMapping("/upload")
    public String uploadLogFile(@RequestParam("logfile") MultipartFile logFile, Model model) {
        if (logFile.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload.");
            return "webpage";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(logFile.getInputStream()))) {
            List<String> logLines = reader.lines().collect(Collectors.toList());

            // Process log counts
            Map<String, Integer> counts = new HashMap<>();
            counts.put("ERROR", countOccurrences(logLines, "ERROR"));
            counts.put("INFO", countOccurrences(logLines, "INFO"));
            counts.put("DEBUG", countOccurrences(logLines, "DEBUG"));
            counts.put("NullPointerException", countOccurrences(logLines, "NullPointerException"));
            counts.put("SchedulerException", countOccurrences(logLines, "SchedulerException"));
            counts.put("AccessException", countOccurrences(logLines, "AccessException"));
            counts.put("InvalidFormatException", countOccurrences(logLines, "InvalidFormatException"));
            counts.put("CloudClientException", countOccurrences(logLines, "CloudClientException"));
            counts.put("ValidationException", countOccurrences(logLines, "ValidationException"));
            counts.put("SuperCsvException", countOccurrences(logLines, "SuperCsvException"));

            // Store logs for later download
            allLogs = logLines;
            detailedErrorLogs = extractDetailedErrorLogs(logLines);

            // Pass data to the view
            model.addAttribute("counts", counts);
            model.addAttribute("allLogs", allLogs);
            model.addAttribute("detailedErrorLogs", detailedErrorLogs);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process the file: " + e.getMessage());
        }

        return "webpage";
    }

    @GetMapping("/downloadErrorLogs")
    public ResponseEntity<InputStreamResource> downloadLogs() throws IOException {
        if (detailedErrorLogs == null) {
            return ResponseEntity.badRequest().body(null);
        }

        // Combine all logs into one string
        StringBuilder sb = new StringBuilder();
        if (detailedErrorLogs != null) {
            sb.append("Detailed Error Logs:\n").append(String.join("\n", detailedErrorLogs)).append("\n\n");
        }

        // Create an in-memory log file
        ByteArrayInputStream in = new ByteArrayInputStream(sb.toString().getBytes());
        InputStreamResource resource = new InputStreamResource(in);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=logs.txt")
            .contentType(MediaType.TEXT_PLAIN)
            .body(resource);
    }

    @GetMapping("/filteredErrorLogs")
    @ResponseBody
    public List<String> filteredErrorLogs(@RequestParam("exceptionType") String exceptionType) {
        if (detailedErrorLogs == null || exceptionType == null || exceptionType.isEmpty()) {
            return Collections.emptyList();
        }

        return detailedErrorLogs.stream()
            .filter(line -> line.contains(exceptionType))
            .collect(Collectors.toList());
    }

    private int countOccurrences(List<String> logLines, String keyword) {
        return (int) logLines.stream().filter(line -> line.contains(keyword)).count();
    }

    private List<String> extractDetailedErrorLogs(List<String> logLines) {
        List<String> detailedLogs = new ArrayList<>();
        boolean isCapturing = false;

        for (String line : logLines) {
            if (line.contains("ERROR")) {
                isCapturing = true;
                detailedLogs.add(line);
            } else if (isCapturing) {
                if (line.isEmpty() || line.startsWith("INFO") || line.startsWith("DEBUG")) {
                    isCapturing = false;
                } else {
                    detailedLogs.add(line);
                }
            }
        }

        return detailedLogs;
    }
}
