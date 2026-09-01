package com.mystikos.booking.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystikos.booking.application.command.AddBookingCartLineCommand;
import com.mystikos.booking.application.service.BookingApplicationService;
import com.mystikos.booking.application.service.BookingCartLineView;
import com.mystikos.common.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** MockMvc standalone，BookingApplicationService 用 Mockito mock——只覆盖 Controller 这层的请求/响应映射。 */
class BookingCartControllerTest {

    private BookingApplicationService bookingApplicationService;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        bookingApplicationService = mock(BookingApplicationService.class);
        BookingCartController controller = new BookingCartController(bookingApplicationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1001", null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addUsesCurrentPatronIdFromSecurityContext() throws Exception {
        when(bookingApplicationService.addToBookingCart(any())).thenReturn(77L);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("companionId", 501);
        payload.put("start", OffsetDateTime.now().plusDays(1).toString());
        payload.put("durationHours", 2.0);

        mockMvc.perform(post("/api/v1/booking-cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(77));

        ArgumentCaptor<AddBookingCartLineCommand> captor = ArgumentCaptor.forClass(AddBookingCartLineCommand.class);
        verify(bookingApplicationService).addToBookingCart(captor.capture());
        assertThat(captor.getValue().patronId()).isEqualTo(1001L);
        assertThat(captor.getValue().companionId()).isEqualTo(501L);
    }

    @Test
    void listReturnsLinesFromService() throws Exception {
        when(bookingApplicationService.listBookingCart(1001L)).thenReturn(List.of(
                new BookingCartLineView(10L, 501L, OffsetDateTime.now(), OffsetDateTime.now().plusHours(2),
                        new BigDecimal("2.0"), new BigDecimal("200.00"), true)));

        mockMvc.perform(get("/api/v1/booking-cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].estimatedPrice").value(200.00));
    }

    @Test
    void removeDelegatesToServiceWithCurrentPatronId() throws Exception {
        mockMvc.perform(delete("/api/v1/booking-cart/10"))
                .andExpect(status().isOk());

        verify(bookingApplicationService).removeFromBookingCart(1001L, 10L);
    }

    @Test
    void checkoutReturnsGroupIdFromService() throws Exception {
        when(bookingApplicationService.checkoutBookingCart(1001L, List.of(10L, 11L))).thenReturn(555L);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("lineIds", List.of(10, 11));

        mockMvc.perform(post("/api/v1/booking-cart/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(555));
    }
}
