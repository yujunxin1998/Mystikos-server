package com.mystikos.identity.infrastructure.persistence;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** identity_user + identity_companion_profile 联表查询的结果行，纯查询用，不是 MyBatis-Plus 实体。 */
@Data
public class CompanionRowPO {
    private Long userId;
    private String phone;
    private String email;
    private String nickname;
    private String avatarUrl;
    private OffsetDateTime createdAt;
    private String level;
    private BigDecimal hourlyRate;
    private String companionStatus;
    private String idCardNo;
    private String bankAccountName;
    private String bankAccountNo;
    private String bankName;
}
