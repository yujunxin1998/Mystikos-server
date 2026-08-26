package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.CompanionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Data
@Schema(description = "管理员编辑打手资料请求，账号字段（手机号/邮箱/密码/昵称）不在此更新，见 UserController")
public class AdminUpdateCompanionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "级别")
    private String level;

    @Schema(description = "擅长游戏标签ID，引用标签目录（category=GAME_TYPE），见 GET /api/v1/tags")
    private Set<Long> tagIds;

    @Schema(description = "小时费率")
    private BigDecimal hourlyRate;

    @Schema(description = "接单状态：AVAILABLE/BUSY/OFFLINE")
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
