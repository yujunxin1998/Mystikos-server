package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.CompanionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "管理员新增打手请求")
public class AdminCreateCompanionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "手机号，与邮箱二选一（至少填一项）")
    private String phone;

    @Schema(description = "邮箱，与手机号二选一（至少填一项）")
    private String email;

    @Schema(description = "密码，为空表示不设置密码（只能用验证码登录）")
    private String password;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "级别")
    private String level;

    @Schema(description = "技能标签")
    private List<String> skillTags;

    @Schema(description = "小时费率")
    private BigDecimal hourlyRate;

    @Schema(description = "接单状态：AVAILABLE/BUSY/OFFLINE，默认 OFFLINE")
    private CompanionStatus status;

    @Schema(description = "身份证号")
    private String idCardNo;

    @Schema(description = "银行开户人姓名")
    private String bankAccountName;

    @Schema(description = "银行卡号")
    private String bankAccountNo;

    @Schema(description = "开户行")
    private String bankName;
}
