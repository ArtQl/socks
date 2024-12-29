package ru.artq.practice.socks.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.artq.practice.socks.errors.CsvProcessingException;
import ru.artq.practice.socks.errors.SocksArgumentException;
import ru.artq.practice.socks.errors.SocksNotFoundException;
import ru.artq.practice.socks.model.Socks;
import ru.artq.practice.socks.repository.SocksRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SocksServiceImpl implements SocksService {
    private final SocksRepository socksRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "socks", key = "#color")
    @Override
    public Integer getSocks(
            String color, String comparison,
            Long cottonPart, Long minCotton,
            Long maxCotton, String sortBy) {
        if (minCotton != null && maxCotton != null && minCotton > maxCotton) {
            throw new SocksArgumentException("Минимальная часть хлопка не может быть больше максимальной");
        }
        List<Socks> socksList = socksRepository
                .findByFilters(color, comparison, cottonPart, minCotton, maxCotton, sortBy);
        if (socksList.isEmpty()) {
            log.warn("Запрос количества носков: носки не найдены для фильтра: color={}, comparison={}, cottonPart={}, minCotton={}, maxCotton={}, sortBy={}", color, comparison, cottonPart, minCotton, maxCotton, sortBy);
            throw new SocksNotFoundException("На складе носки не найдены");
        }
        log.info("Запрос количества носков: найдено {} носков для фильтра: color={}, comparison={}, cottonPart={}, minCotton={}, maxCotton={}, sortBy={}", socksList, color, comparison, cottonPart, minCotton, maxCotton, sortBy);
        return socksList.stream().mapToInt(Socks::getQuantity).sum();
    }

    @Override
    public void registerIncome(String color, Long cottonPart, Integer quantity) {
        checkParams(color, cottonPart, quantity);
        Socks socks = findByColorAndCottonPart(color, cottonPart);
        socks.setQuantity(socks.getQuantity() + quantity);
        socksRepository.save(socks);
        log.info("Приход носков: color={}, cottonPart={}, quantity={}", color, cottonPart, quantity);

    }

    @Override
    public void registerOutcome(String color, Long cottonPart, Integer quantity) {
        checkParams(color, cottonPart, quantity);
        Socks socks = findByColorAndCottonPart(color, cottonPart);
        if (socks.getQuantity() < quantity) {
            log.warn("Отпуск носков: недостаточно носков на складе для color={}, cottonPart={}, требуемое количество: {}", color, cottonPart, quantity);
            throw new SocksNotFoundException("Нехватка носков на складе");
        }
        socks.setQuantity(socks.getQuantity() - quantity);
        socksRepository.save(socks);
        log.info("Отпуск носков: color={}, cottonPart={}, quantity={}", color, cottonPart, quantity);

    }

    @Override
    public void updateSocks(Long id, String color, Long cottonPart, Integer quantity) {
        Socks socks = socksRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Обновление носков: носки с id={} не найдены", id);
                    return new SocksNotFoundException("На складе носки не найдены");
                });
        checkParams(color, cottonPart, quantity);
        socks.setColor(color);
        socks.setCottonPart(cottonPart);
        socks.setQuantity(quantity);
        socksRepository.save(socks);
        log.info("Обновление носков: id={}, color={}, cottonPart={}, quantity={}", id, color, cottonPart, quantity);

    }

    @Override
    public void batchSocks(MultipartFile file) {
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = csvReader.readAll();
            records.forEach(record -> {
                try {
                    if (record.length != 3) {
                        throw new SocksArgumentException("Некорректный формат данных: " + String.join(", ", record));
                    }
                    String color = record[0].trim();
                    Long cottonPart = Long.parseLong(record[1].trim());
                    Integer quantity = Integer.parseInt(record[2].trim());
                    registerIncome(color, cottonPart, quantity);
                } catch (NumberFormatException | SocksArgumentException e) {
                    log.error("Ошибка обработки записи: {}", String.join(", ", record), e);
                    throw new CsvProcessingException("Ошибки при обработке файла: ", e);
                }
            });
            log.info("Загрузка партий носков завершена успешно, количество партий: {}", records.size());
        } catch (IOException | CsvException e) {
            log.error("Ошибка при обработке CSV файла", e);
            throw new CsvProcessingException("Ошибки при обработке файлов: ", e);
        }
    }

    @Override
    public Socks getAnySocks() {
        long count = new Random().nextLong(1, socksRepository.findCount());
        Socks socks = socksRepository.findById(count).orElse(new Socks());
        log.info("Запрос количества носков: найдено {}", socks);
        return socks;
    }

    private Socks findByColorAndCottonPart(String color, Long cottonPart) {
        return socksRepository.findByColorAndCottonPart(color, cottonPart)
                .orElseGet(() -> {
                    log.info("Создание новой записи для color={}, cottonPart={}", color, cottonPart);
                    return new Socks(color, cottonPart, 0);
                });

    }

    void checkParams(String color, Long cottonPart, Integer quantity) {
        validateCondition(!color.isEmpty(), "Цвет не может быть пустым");
        validateCondition(cottonPart > 0, "Часть хлопка должна быть больше 0");
        validateCondition(quantity > 0, "Количество должно быть больше 0");
    }

    private void validateCondition(Boolean condition, String message) {
        if (!condition) {
            log.warn("Ошибка проверки параметров: {}", message);
            throw new SocksArgumentException(message);
        }
    }
}
