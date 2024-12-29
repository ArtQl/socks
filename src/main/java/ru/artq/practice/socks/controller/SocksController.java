package ru.artq.practice.socks.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.artq.practice.socks.model.Socks;
import ru.artq.practice.socks.service.SocksService;

@RestController
@RequestMapping("api/socks")
@RequiredArgsConstructor
public class SocksController {
    private final SocksService socksService;

    @Operation(summary = "Получить общие количество носков с фильтрацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Успешный запрос",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Integer.class)
                            , examples = @ExampleObject(value = "100"))
            ),
            @ApiResponse(responseCode = "404", description = "На складе носки не найден", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Integer> getSocksQuantity(
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String comparison,
            @RequestParam(required = false) Long cottonPart,
            @RequestParam(required = false) Long minCotton,
            @RequestParam(required = false) Long maxCotton,
            @RequestParam(required = false) String sortBy) {
        Integer result = socksService.getSocks(
                color, comparison, cottonPart, minCotton, maxCotton, sortBy);
        return ResponseEntity.ok(result);
    }

    @GetMapping("any")
    public ResponseEntity<Socks> getAnySocks() {
        return ResponseEntity.ok(socksService.getAnySocks());
    }

    @Operation(summary = "Регистрация прихода носков")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Приход носков зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")

    })
    @PostMapping("income")
    public ResponseEntity<Void> registerIncome(
            @RequestParam String color,
            @RequestParam Long cottonPart,
            @RequestParam Integer quantity) {
        socksService.registerIncome(color, cottonPart, quantity);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Регистрация отпуска носков")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отпуск носков зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "Нехватка носков на складе"),
    })
    @PostMapping("outcome")
    public ResponseEntity<Void> registerOutcome(
            @RequestParam String color,
            @RequestParam Long cottonPart,
            @RequestParam Integer quantity) {
        socksService.registerOutcome(color, cottonPart, quantity);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновить данные носков")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные носков обновлены"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "404", description = "На складе носки не найдены")
    })
    @PutMapping("{id}")
    public ResponseEntity<Void> updateSocks(
            @PathVariable Long id,
            @RequestParam String color,
            @RequestParam Long cottonPart,
            @RequestParam Integer quantity) {
        socksService.updateSocks(id, color, cottonPart, quantity);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Загрузка партий носков из CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Партии носков успешно загружены"),
            @ApiResponse(responseCode = "400", description = "Некорректный формат данных в файле: должно быть 3 аргумента"),
            @ApiResponse(responseCode = "500", description = "Ошибка при обработке CSV файла")
    })
    @PostMapping("batch")
    public ResponseEntity<Void> batchSocks(@RequestParam("file") MultipartFile file) {
        socksService.batchSocks(file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
