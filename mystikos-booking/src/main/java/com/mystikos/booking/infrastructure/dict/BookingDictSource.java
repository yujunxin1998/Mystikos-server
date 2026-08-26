package com.mystikos.booking.infrastructure.dict;

import com.mystikos.booking.domain.model.BookingStatus;
import com.mystikos.common.dict.DictCatalog;
import com.mystikos.common.dict.DictSource;
import org.springframework.stereotype.Component;

import java.util.List;

/** Booking 上下文贡献进全局字典接口的枚举，见 {@code mystikos-system-operation} 的 DictionaryController。 */
@Component
public class BookingDictSource implements DictSource {

    @Override
    public List<DictCatalog> dictCatalogs() {
        return List.of(DictCatalog.of("BOOKING_STATUS", "预约订单状态", BookingStatus.values()));
    }
}
