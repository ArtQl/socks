package ru.artq.practice.socks.service;

import org.springframework.web.multipart.MultipartFile;

public interface SocksService {


    Integer getSocksQuantity(String color, String comparison, Long cottonPart, Long minCotton, Long maxCotton, String sortBy);

    void registerIncome(String color, Long cottonPart, Integer quantity);

    void registerOutcome(String color, Long cottonPart, Integer quantity);

    void updateSocks(Long id, String color, Long cottonPart, Integer quantity);

    void batchSocks(MultipartFile file);
}
