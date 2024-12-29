package ru.artq.practice.socks.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.artq.practice.socks.errors.CsvProcessingException;
import ru.artq.practice.socks.errors.SocksArgumentException;
import ru.artq.practice.socks.errors.SocksNotFoundException;
import ru.artq.practice.socks.model.Socks;
import ru.artq.practice.socks.repository.SocksRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocksServiceImplTest {

    @Mock
    private SocksRepository socksRepository;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private SocksServiceImpl socksService;

    private Socks socks;

    @BeforeEach
    void setUp() {
        socks = new Socks("blue", 80L, 10);
    }

    @Test
    void test1_getSocksQuantity_shouldReturnCorrect() {
        when(socksRepository.findByFilters("blue", "equal",
                80L, null, null, "asc"))
                .thenReturn(List.of(socks));

        Integer result = socksService.getSocks("blue", "equal",
                80L, null, null, "asc");

        assertEquals(10, result);
        verify(socksRepository, times(1))
                .findByFilters(any(), any(), any(), any(), any(), any());
    }

    @Test
    void test2_getSocksQuantity_shouldThrowSocksNotFoundException_whenNoSocksFound() {
        when(socksRepository.findByFilters("blue", "=", 80L, null, null, "asc"))
                .thenReturn(List.of());

        SocksNotFoundException exception = assertThrows(SocksNotFoundException.class, () ->
                socksService.getSocks("blue", "=", 80L, null, null, "asc"));
        assertEquals("На складе носки не найдены", exception.getMessage());
    }

    @Test
    void test3_registerIncome_shouldIncreaseQuantity() {
        when(socksRepository.findByColorAndCottonPart("blue", 80L))
                .thenReturn(Optional.of(socks));

        socksService.registerIncome("blue", 80L, 5);

        assertEquals(15, socks.getQuantity());
        verify(socksRepository, times(1)).save(socks);
    }

    @Test
    void test4_registerOutcome_shouldDecreaseQuantity() {
        when(socksRepository.findByColorAndCottonPart("blue", 80L))
                .thenReturn(Optional.of(socks));

        socksService.registerOutcome("blue", 80L, 5);

        assertEquals(5, socks.getQuantity());
        verify(socksRepository, times(1)).save(socks);
    }

    @Test
    void test5_registerOutcome_shouldThrowSocksNotFoundException_whenNotEnoughSocks() {
        when(socksRepository.findByColorAndCottonPart("blue", 80L))
                .thenReturn(Optional.of(socks));

        SocksNotFoundException exception = assertThrows(SocksNotFoundException.class, () ->
                socksService.registerOutcome("blue", 80L, 15));
        assertEquals("Нехватка носков на складе", exception.getMessage());
    }

    @Test
    void test6_updateSocks_shouldUpdateSocks() {
        when(socksRepository.findById(1L))
                .thenReturn(Optional.of(socks));

        socksService.updateSocks(1L, "green", 90L, 20);

        assertEquals("green", socks.getColor());
        assertEquals(90L, socks.getCottonPart());
        assertEquals(20, socks.getQuantity());
        verify(socksRepository, times(1)).save(socks);
    }

    @Test
    void test7_updateSocks_shouldThrowSocksNotFoundException_whenSocksNotFound() {
        when(socksRepository.findById(1L))
                .thenReturn(Optional.empty());

        SocksNotFoundException exception = assertThrows(SocksNotFoundException.class, () ->
                socksService.updateSocks(1L, "green", 90L, 20));
        assertEquals("На складе носки не найдены", exception.getMessage());
    }

    @Test
    void test8_batchSocks_shouldProcessCSVFile() throws IOException {
        String csvData = "blue,80,10\ngreen,70,15";
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csvData.getBytes()));

        socksService.batchSocks(file);

        verify(socksRepository, times(2)).save(any(Socks.class));
    }

    @Test
    void test9_batchSocks_shouldThrowCsvProcessingException_whenInvalidData() throws IOException {
        String csvData = "blue,80,invalid\n";
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csvData.getBytes()));

        CsvProcessingException exception = assertThrows(CsvProcessingException.class, () ->
                socksService.batchSocks(file));
        assertEquals("Ошибки при обработке файла: ", exception.getMessage());
    }

    @Test
    void test10_checkParams_shouldThrowSocksArgumentException_whenInvalidParams() {
        SocksArgumentException exception = assertThrows(SocksArgumentException.class, () ->
                socksService.checkParams("", -1L, -5));
        assertEquals("Цвет не может быть пустым", exception.getMessage());
    }
}