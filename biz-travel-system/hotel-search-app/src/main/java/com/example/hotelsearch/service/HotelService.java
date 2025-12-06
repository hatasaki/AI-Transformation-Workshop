package com.example.hotelsearch.service;

import com.example.hotelsearch.model.Hotel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class HotelService {

    private final Path storagePath;
    private final ObjectMapper mapper = new ObjectMapper();

    public HotelService(@Value("${hotel.storage:data/hotels.json}") String storageLocation) {
        this.storagePath = Path.of(storageLocation);
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(storagePath.getParent());
        if (Files.notExists(storagePath)) {
            List<Hotel> seed = List.of(
                    new Hotel("Central Station Inn", "Tokyo", new BigDecimal("12000"), "駅に近い, 朝食付き"),
                    new Hotel("Bay View Hotel", "Yokohama", new BigDecimal("9500"), "海沿い, 駅に近い"),
                    new Hotel("Mountain Lodge", "Nagano", new BigDecimal("8000"), "自然, 温泉"),
                    new Hotel("City Comfort", "Osaka", new BigDecimal("11000"), "駅に近い, ビジネス向け")
            );
            Files.createFile(storagePath);
            mapper.writerWithDefaultPrettyPrinter().writeValue(storagePath.toFile(), seed);
        }
    }

    public List<Hotel> search(String city, BigDecimal maxPrice, String feature) {
        List<Hotel> all = loadAll();
        return all.stream()
                .filter(h -> city == null || h.getCity().equalsIgnoreCase(city))
                .filter(h -> maxPrice == null || h.getPricePerNight().compareTo(maxPrice) <= 0)
                .filter(h -> feature == null || feature.isBlank() || containsFeature(h.getFeatures(), feature))
                .collect(Collectors.toList());
    }

    private List<Hotel> loadAll() {
        try {
            if (Files.size(storagePath) == 0) {
                return Collections.emptyList();
            }
            return mapper.readValue(storagePath.toFile(), new TypeReference<List<Hotel>>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read hotels", e);
        }
    }

    private boolean containsFeature(String features, String expected) {
        if (features == null || expected == null) {
            return false;
        }
        String normalized = features.toLowerCase(Locale.ROOT);
        for (String token : expected.toLowerCase(Locale.ROOT).split(",")) {
            if (normalized.contains(token.trim())) {
                return true;
            }
        }
        return false;
    }
}
