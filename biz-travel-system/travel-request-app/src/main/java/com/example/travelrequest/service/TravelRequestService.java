package com.example.travelrequest.service;

import com.example.travelrequest.model.TravelRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TravelRequestService {

    private final ObjectMapper objectMapper;
    private final Path storagePath;

    public TravelRequestService(@Value("${travel.requests.storage:data/travel-requests.json}") String storageLocation) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.storagePath = Path.of(storageLocation);
    }

    @PostConstruct
    void prepareStorage() throws IOException {
        Files.createDirectories(storagePath.getParent());
        if (Files.notExists(storagePath)) {
            Files.createFile(storagePath);
            saveAll(new ArrayList<>());
        }
    }

    public synchronized List<TravelRequest> findAll() {
        try {
            if (Files.size(storagePath) == 0) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(storagePath.toFile(), new TypeReference<List<TravelRequest>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read travel requests", e);
        }
    }

    public synchronized TravelRequest create(TravelRequest request) {
        List<TravelRequest> current = findAll();
        UUID id = request.getId() != null ? request.getId() : UUID.randomUUID();
        request.setId(id);
        current.add(request);
        saveAll(current);
        return request;
    }

    private void saveAll(List<TravelRequest> requests) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(storagePath.toFile(), requests);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to persist travel requests", e);
        }
    }
}
