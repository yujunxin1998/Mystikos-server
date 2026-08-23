package com.mystikos.identity.adapter.web.dto;

import com.mystikos.identity.domain.model.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Schema(description = "更新老板资料请求")
public class UpdateProfileRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "性别，不填默认不愿透露")
    private Gender gender;

    @Schema(description = "头像地址，先通过 /api/v1/files/upload 拿到 URL 再填这里")
    private String avatarUrl;

    @Schema(description = "生日，仅作资料展示，不作为年龄/实名认证依据")
    private LocalDate birthDate;

    @Schema(description = "个性签名")
    private String bio;

    @Schema(description = "所在地区编码，引用 common_region.code（国家或一级行政区），见 GET /api/v1/regions/tree")
    private String regionCode;
}
