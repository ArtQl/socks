package ru.artq.practice.socks.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.artq.practice.socks.model.Socks;
import ru.artq.practice.socks.service.SocksService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class SocksControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SocksService socksService;

    @InjectMocks
    private SocksController socksController;


    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(socksController).build();
    }

    @Test
    void test1_testGetSocksQuantity() throws Exception {
        String color = "blue";
        String comparison = "moreThen";
        Long cottonPart = 80L;
        Long minCotton = 30L;
        Long maxCotton = 90L;
        String sortBy = "color";
        Integer expectedQuantity = 100;
        when(socksService.getSocks(color, comparison, cottonPart, minCotton, maxCotton, sortBy))
                .thenReturn(expectedQuantity);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/socks")
                        .param("color", color)
                        .param("comparison", comparison)
                        .param("cottonPart", cottonPart.toString())
                        .param("minCotton", minCotton.toString())
                        .param("maxCotton", maxCotton.toString())
                        .param("sortBy", sortBy))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(String.valueOf(expectedQuantity)));
    }

    @Test
    void test2_testRegisterIncome() throws Exception {
        String color = "blue";
        Long cottonPart = 80L;
        Integer quantity = 100;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/socks/income")
                        .param("color", color)
                        .param("cottonPart", cottonPart.toString())
                        .param("quantity", quantity.toString()))
                .andExpect(status().isOk());

        verify(socksService, times(1)).registerIncome(color, cottonPart, quantity);
    }

    @Test
    void test3_testRegisterOutcome() throws Exception {
        String color = "blue";
        Long cottonPart = 80L;
        Integer quantity = 50;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/socks/outcome")
                        .param("color", color)
                        .param("cottonPart", cottonPart.toString())
                        .param("quantity", quantity.toString()))
                .andExpect(status().isOk());

        verify(socksService, times(1)).registerOutcome(color, cottonPart, quantity);
    }

    @Test
    void test4_testUpdateSocks() throws Exception {
        Long id = 1L;
        String color = "blue";
        Long cottonPart = 80L;
        Integer quantity = 150;

        mockMvc.perform(MockMvcRequestBuilders.put("/api/socks/{id}", id)
                        .param("color", color)
                        .param("cottonPart", cottonPart.toString())
                        .param("quantity", quantity.toString()))
                .andExpect(status().isOk());

        verify(socksService, times(1)).updateSocks(id, color, cottonPart, quantity);
    }
}