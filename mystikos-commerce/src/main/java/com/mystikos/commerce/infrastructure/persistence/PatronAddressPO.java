package com.mystikos.commerce.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Schema(description = "老板收货地址持久化对象")
@TableName("commerce_patron_address")
public class PatronAddressPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "老板用户ID")
    @TableField("patron_id")
    private Long patronId;

    @Schema(description = "地址类型：DOMESTIC/OVERSEAS")
    @TableField("address_type")
    private String addressType;

    @Schema(description = "收件人姓名")
    @TableField("recipient_name")
    private String recipientName;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "国家编码")
    @TableField("country_code")
    private String countryCode;

    @Schema(description = "省份编码，仅国内地址")
    @TableField("province_code")
    private String provinceCode;

    @Schema(description = "城市")
    @TableField("city")
    private String city;

    @Schema(description = "区/县，仅国内地址")
    @TableField("district")
    private String district;

    @Schema(description = "详细地址")
    @TableField("address_line1")
    private String addressLine1;

    @Schema(description = "详细地址补充")
    @TableField("address_line2")
    private String addressLine2;

    @Schema(description = "州/大区，仅海外地址可选")
    @TableField("state_region")
    private String stateRegion;

    @Schema(description = "邮政编码")
    @TableField("postal_code")
    private String postalCode;

    @Schema(description = "是否默认地址")
    @TableField("is_default")
    private Boolean isDefault;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private OffsetDateTime updatedAt;
}
