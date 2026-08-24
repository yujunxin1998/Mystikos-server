package com.mystikos.identity.adapter.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
@Schema(description = "保存陪玩名片草稿请求")
public class SaveCompanionShowcaseDraftRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "自我介绍，一两百字以内")
    @Size(max = 300)
    private String bio;

    @Schema(description = "一句话标签，展示在名片名称旁")
    @Size(max = 50)
    private String tagline;

    @Schema(description = "可约状态文案，纯展示（如\"今晚可约\"），不是系统接单状态")
    @Size(max = 20)
    private String availability;

    @Schema(description = "游戏标签ID，引用标签目录（category=GAME_TYPE）")
    private Set<Long> tagIds;

    @Schema(description = "名片横向背景图，取 /api/v1/files/upload 返回的 objectKey")
    private String coverObjectKey;

    @Schema(description = "照片，取 /api/v1/files/upload 返回的 objectKey 列表，最多9张，按顺序展示")
    @Size(max = 9)
    private List<String> photoObjectKeys;

    @Schema(description = "精彩视频，取 /api/v1/files/upload 返回的 objectKey 列表，最多3个")
    @Size(max = 5)
    private List<String> videoObjectKeys;

    @Schema(description = "语音，取 /api/v1/files/upload 返回的 objectKey 列表，最多3个")
    @Size(max = 3)
    private List<String> audioObjectKeys;
}
