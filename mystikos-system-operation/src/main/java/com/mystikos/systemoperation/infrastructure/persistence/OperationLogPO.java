package com.mystikos.systemoperation.infrastructure.persistence;

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
@Schema(description = "后台操作日志持久化对象")
@TableName("sysop_operation_log")
public class OperationLogPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "操作人用户ID，拿不到时为空")
    @TableField("operator_id")
    private String operatorId;

    @Schema(description = "HTTP方法")
    @TableField("http_method")
    private String httpMethod;

    @Schema(description = "请求路径")
    @TableField("request_path")
    private String requestPath;

    @Schema(description = "查询字符串")
    @TableField("query_string")
    private String queryString;

    @Schema(description = "请求体（截断）")
    @TableField("request_body")
    private String requestBody;

    @Schema(description = "响应状态码")
    @TableField("response_status")
    private Integer responseStatus;

    @Schema(description = "是否成功")
    @TableField("success")
    private Boolean success;

    @Schema(description = "异常信息（如果有）")
    @TableField("error_message")
    private String errorMessage;

    @Schema(description = "客户端IP")
    @TableField("client_ip")
    private String clientIp;

    @Schema(description = "耗时（毫秒）")
    @TableField("duration_ms")
    private Long durationMs;

    @Schema(description = "发生时间")
    @TableField("created_at")
    private OffsetDateTime createdAt;
}
