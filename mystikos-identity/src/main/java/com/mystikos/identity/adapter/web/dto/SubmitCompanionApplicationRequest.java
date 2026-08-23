package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Data
@Schema(description = "提交陪玩身份申请请求")
public class SubmitCompanionApplicationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "真实姓名")
    @NotBlank
    private String realName;

    @Schema(description = "性别，不填默认不愿透露")
    private Gender gender;

    @Schema(description = "出生日期")
    private LocalDate birthDate;

    @Schema(description = "自我介绍/擅长说明")
    private String selfIntro;

    @Schema(description = "游戏昵称")
    @NotBlank
    private String gameNickname;

    @Schema(description = "段位截图等游戏相关资料，取 /api/v1/files/upload 返回的 objectKey，可不填")
    private String gameRankProofObjectKey;

    @Schema(description = "擅长游戏类型标签ID，引用标签目录（category=GAME_TYPE），至少选一个")
    @NotEmpty
    private Set<Long> tagIds;

    @Schema(description = "联系手机号的国家区号，如 +86；填手机号时必须一并提供")
    private String contactCountryCode;

    @Schema(description = "联系手机号，与联系邮箱二选一（至少填一项），不等同于账号绑定的手机号")
    private String contactPhone;

    @Schema(description = "联系邮箱，与联系手机号二选一（至少填一项），不等同于账号绑定的邮箱")
    private String contactEmail;
}
