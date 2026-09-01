package com.mystikos.booking.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystikos.booking.application.port.PaymentCheckoutResult;
import com.mystikos.booking.application.service.BookingApplicationService;
import com.mystikos.booking.application.service.BookingOrderGroupView;
import com.mystikos.booking.domain.model.BookingGroupStatus;
import com.mystikos.common.web.exception.GlobalExceptionHandler;
import com.mystikos.payment.application.port.PaymentPayloadType;
import com.mystikos.payment.application.port.PaymentScene;
import com.mystikos.payment.domain.model.PaymentProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** MockMvc standalone，BookingApplicationService 用 Mockito mock——只覆盖 Controller 这层的请求/响应映射。 */
class BookingGroupControllerTest {

    private BookingApplicationService bookingApplicationService;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        bookingApplicationService = mock(BookingApplicationService.class);
        BookingGroupController controller = new BookingGroupController(bookingApplicationService);
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
    void getReturnsGroupViewFromService() throws Exception {
        when(bookingApplicationService.getBookingGroup(400L, 1001L)).thenReturn(new BookingOrderGroupView(
                400L, BookingGroupStatus.PENDING_PAYMENT, new BigDecimal("350.00"), OffsetDateTime.now(),
                OffsetDateTime.now().plusMinutes(15), List.of()));

        mockMvc.perform(get("/api/v1/booking-groups/400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(400))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.data.totalAmount").value(350.00));
    }

    @Test
    void requestPaymentPassesProviderAndSceneThrough() throws Exception {
        when(bookingApplicationService.requestGroupPayment(eq(400L), eq(1001L), eq(PaymentProvider.WECHAT_PAY), eq(PaymentScene.WAP_H5)))
                .thenReturn(new PaymentCheckoutResult(999L, PaymentPayloadType.REDIRECT_URL,
                        Map.of("redirectUrl", "https://pay.example/x"), "CREATED"));

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("provider", "WECHAT_PAY");
        payload.put("scene", "WAP_H5");

        mockMvc.perform(post("/api/v1/booking-groups/400/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.intentId").value(999))
                .andExpect(jsonPath("$.data.payloadType").value("REDIRECT_URL"));
    }

    @Test
    void cancelDelegatesToServiceWithCurrentPatronId() throws Exception {
        mockMvc.perform(post("/api/v1/booking-groups/400/cancel"))
                .andExpect(status().isOk());

        verify(bookingApplicationService).cancelGroup(400L, 1001L);
    }
}
