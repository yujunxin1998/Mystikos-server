package com.mystikos.commerce.domain.model;

import com.mystikos.commerce.domain.CommerceException;

/**
 * 老板收货地址簿里的一条地址。国内/海外要求的字段集不同，校验规则收在 {@link #validate()}
 * 里，任何调用方都不能绕过校验直接构造出一个字段不齐全的地址——同 {@link CartItem} 的做法。
 */
public class PatronAddress {

    private Long id;
    private final Long patronId;
    private AddressType addressType;
    private String recipientName;
    private String phone;
    private String countryCode;
    private String provinceCode;
    private String city;
    private String district;
    private String addressLine1;
    private String addressLine2;
    private String stateRegion;
    private String postalCode;
    private boolean isDefault;

    private PatronAddress(Long id, Long patronId, AddressType addressType, String recipientName, String phone,
                           String countryCode, String provinceCode, String city, String district,
                           String addressLine1, String addressLine2, String stateRegion, String postalCode,
                           boolean isDefault) {
        this.id = id;
        this.patronId = patronId;
        this.addressType = addressType;
        this.recipientName = recipientName;
        this.phone = phone;
        this.countryCode = countryCode;
        this.provinceCode = provinceCode;
        this.city = city;
        this.district = district;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.stateRegion = stateRegion;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    public static PatronAddress create(Long patronId, AddressType addressType, String recipientName, String phone,
                                        String countryCode, String provinceCode, String city, String district,
                                        String addressLine1, String addressLine2, String stateRegion,
                                        String postalCode, boolean isDefault) {
        PatronAddress address = new PatronAddress(null, patronId, addressType, recipientName, phone, countryCode,
                provinceCode, city, district, addressLine1, addressLine2, stateRegion, postalCode, isDefault);
        address.validate();
        return address;
    }

    public static PatronAddress restore(Long id, Long patronId, AddressType addressType, String recipientName,
                                         String phone, String countryCode, String provinceCode, String city,
                                         String district, String addressLine1, String addressLine2,
                                         String stateRegion, String postalCode, boolean isDefault) {
        return new PatronAddress(id, patronId, addressType, recipientName, phone, countryCode, provinceCode, city,
                district, addressLine1, addressLine2, stateRegion, postalCode, isDefault);
    }

    /** 用新值整体覆盖并重新校验；跟 create 走同一套字段要求，不允许改出一个不合法的状态。 */
    public void update(AddressType addressType, String recipientName, String phone, String countryCode,
                        String provinceCode, String city, String district, String addressLine1, String addressLine2,
                        String stateRegion, String postalCode) {
        this.addressType = addressType;
        this.recipientName = recipientName;
        this.phone = phone;
        this.countryCode = countryCode;
        this.provinceCode = provinceCode;
        this.city = city;
        this.district = district;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.stateRegion = stateRegion;
        this.postalCode = postalCode;
        validate();
    }

    /**
     * 国内地址：国家固定 CN，省/市/区必填，不允许填 stateRegion（没有对应概念）。
     * 海外地址：国家/城市必填，不允许填省（province_code 是国内行政区划编码，海外地址用不上）。
     */
    private void validate() {
        if (recipientName == null || recipientName.isBlank()) {
            throw CommerceException.addressInvalid("收件人姓名不能为空");
        }
        if (phone == null || phone.isBlank()) {
            throw CommerceException.addressInvalid("联系电话不能为空");
        }
        if (addressLine1 == null || addressLine1.isBlank()) {
            throw CommerceException.addressInvalid("详细地址不能为空");
        }
        if (addressType == AddressType.DOMESTIC) {
            if (!"CN".equals(countryCode)) {
                throw CommerceException.addressInvalid("国内地址的国家必须是 CN");
            }
            if (provinceCode == null || provinceCode.isBlank()) {
                throw CommerceException.addressInvalid("国内地址必须选择省份");
            }
            if (city == null || city.isBlank()) {
                throw CommerceException.addressInvalid("国内地址必须填写城市");
            }
            if (district == null || district.isBlank()) {
                throw CommerceException.addressInvalid("国内地址必须填写区/县");
            }
            if (stateRegion != null && !stateRegion.isBlank()) {
                throw CommerceException.addressInvalid("国内地址不需要填写州/大区");
            }
        } else {
            if (countryCode == null || countryCode.isBlank()) {
                throw CommerceException.addressInvalid("海外地址必须选择国家");
            }
            if (city == null || city.isBlank()) {
                throw CommerceException.addressInvalid("海外地址必须填写城市");
            }
            if (provinceCode != null && !provinceCode.isBlank()) {
                throw CommerceException.addressInvalid("海外地址不适用国内省份编码");
            }
            if (district != null && !district.isBlank()) {
                throw CommerceException.addressInvalid("海外地址不适用国内区/县字段");
            }
        }
    }

    /** 格式化成下单时快照进 MerchandiseOrder.shippingAddress 的单行文本。 */
    public String formatForSnapshot() {
        StringBuilder sb = new StringBuilder();
        sb.append(recipientName).append(' ').append(phone).append(' ');
        if (addressType == AddressType.DOMESTIC) {
            sb.append(city).append(district);
        } else {
            sb.append(city);
            if (stateRegion != null && !stateRegion.isBlank()) {
                sb.append(' ').append(stateRegion);
            }
            sb.append(' ').append(countryCode);
        }
        sb.append(' ').append(addressLine1);
        if (addressLine2 != null && !addressLine2.isBlank()) {
            sb.append(' ').append(addressLine2);
        }
        if (postalCode != null && !postalCode.isBlank()) {
            sb.append(' ').append(postalCode);
        }
        return sb.toString();
    }

    public void markDefault() {
        this.isDefault = true;
    }

    public void unmarkDefault() {
        this.isDefault = false;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getPatronId() {
        return patronId;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhone() {
        return phone;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getStateRegion() {
        return stateRegion;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public boolean isDefault() {
        return isDefault;
    }
}
