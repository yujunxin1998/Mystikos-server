package com.mystikos.commerce.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystikos.commerce.application.service.AddressApplicationService;
import com.mystikos.commerce.domain.model.AddressType;
import com.mystikos.commerce.domain.model.PatronAddress;
import com.mystikos.commerce.domain.repository.PatronAddressRepository;
import com.mystikos.common.region.RegionQueryService;
import com.mystikos.common.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc standalone，但 AddressApplicationService 是真实实例（只有 PatronAddressRepository/
 * RegionQueryService 是 Mockito mock）——这样国内/海外地址的字段校验（PatronAddress#validate）
 * 是真的在跑，不是被 mock 掉的空壳断言。
 */
class AddressControllerTest {

    private PatronAddressRepository patronAddressRepository;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        patronAddressRepository = mock(PatronAddressRepository.class);
        RegionQueryService regionQueryService = mock(RegionQueryService.class);
        when(regionQueryService.exists(any())).thenReturn(true);

        AtomicLong idSequence = new AtomicLong(1);
        when(patronAddressRepository.save(any())).thenAnswer(invocation -> {
            PatronAddress address = invocation.getArgument(0);
            if (address.getId() == null) {
                address.assignId(idSequence.getAndIncrement());
            }
            return address;
        });

        AddressApplicationService service = new AddressApplicationService(patronAddressRepository, regionQueryService);
        AddressController controller = new AddressController(service);
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
    void createDomesticAddressWithAllRequiredFieldsSucceeds() throws Exception {
        mockMvc.perform(post("/api/v1/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(domesticAddressPayload())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void createDomesticAddressMissingProvinceIsRejectedWithFieldInvalidCode() throws Exception {
        Map<String, Object> payload = domesticAddressPayload();
        payload.remove("provinceCode");

        mockMvc.perform(post("/api/v1/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(6010));
    }

    @Test
    void createOverseasAddressWithProvinceCodeIsRejected() throws Exception {
        // 海外地址不适用国内省份编码——填了就该被拒，不能悄悄忽略。
        Map<String, Object> payload = overseasAddressPayload();
        payload.put("provinceCode", "DE-BY");

        mockMvc.perform(post("/api/v1/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(6010));
    }

    @Test
    void createOverseasAddressWithAllRequiredFieldsSucceeds() throws Exception {
        mockMvc.perform(post("/api/v1/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overseasAddressPayload())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listReturnsOnlyCurrentPatronsAddresses() throws Exception {
        PatronAddress address = PatronAddress.create(1001L, AddressType.OVERSEAS, "Jane Doe", "+49123456789",
                "DE", null, "Berlin", null, "Musterstr. 1", null, "Berlin", "10115", false);
        address.assignId(7L);
        when(patronAddressRepository.findAllByPatron(1001L)).thenReturn(List.of(address));

        mockMvc.perform(get("/api/v1/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(7))
                .andExpect(jsonPath("$.data[0].addressType").value("OVERSEAS"));
    }

    @Test
    void deleteSomeoneElsesAddressIsRejectedAsNotFound() throws Exception {
        PatronAddress address = PatronAddress.create(2002L, AddressType.OVERSEAS, "Someone Else", "+10000000000",
                "DE", null, "Berlin", null, "Musterstr. 2", null, null, null, false);
        address.assignId(9L);
        when(patronAddressRepository.findById(9L)).thenReturn(Optional.of(address));

        mockMvc.perform(delete("/api/v1/addresses/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(6009));
    }

    private Map<String, Object> domesticAddressPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("addressType", "DOMESTIC");
        payload.put("recipientName", "张三");
        payload.put("phone", "13800000000");
        payload.put("countryCode", "CN");
        payload.put("provinceCode", "CN-GD");
        payload.put("city", "深圳市");
        payload.put("district", "南山区");
        payload.put("addressLine1", "科技园路1号");
        payload.put("setDefault", false);
        return payload;
    }

    private Map<String, Object> overseasAddressPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("addressType", "OVERSEAS");
        payload.put("recipientName", "Jane Doe");
        payload.put("phone", "+49123456789");
        payload.put("countryCode", "DE");
        payload.put("city", "Berlin");
        payload.put("addressLine1", "Musterstr. 1");
        payload.put("stateRegion", "Berlin");
        payload.put("postalCode", "10115");
        payload.put("setDefault", false);
        return payload;
    }
}
