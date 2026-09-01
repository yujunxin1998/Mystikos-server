package com.mystikos.commerce.application.command;

import com.mystikos.commerce.domain.model.AddressType;

public record UpdateAddressCommand(AddressType addressType, String recipientName, String phone, String countryCode,
                                    String provinceCode, String city, String district, String addressLine1,
                                    String addressLine2, String stateRegion, String postalCode,
                                    boolean setDefault) {
}
