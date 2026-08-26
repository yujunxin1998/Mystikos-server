-- 系统运营上下文：系统配置内容文档（法律条款等，可后台编辑，带修订历史）+ 后台操作日志。
-- 字典功能不建表，直接聚合各上下文的枚举，见 mystikos-common 的 com.mystikos.common.dict 包。

CREATE TABLE sysop_document (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    updated_by VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE sysop_document IS '系统配置内容文档，code 自由字符串，如 TERMS_OF_SERVICE/PRIVACY_POLICY';

CREATE TABLE sysop_document_revision (
    id BIGSERIAL PRIMARY KEY,
    document_code VARCHAR(64) NOT NULL,
    version INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    updated_by VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE sysop_document_revision IS 'sysop_document 每次更新的只追加快照，供后台查看历史';
CREATE INDEX idx_sysop_document_revision_code ON sysop_document_revision(document_code, version DESC);

CREATE TABLE sysop_operation_log (
    id BIGSERIAL PRIMARY KEY,
    operator_id VARCHAR(64),
    http_method VARCHAR(10) NOT NULL,
    request_path VARCHAR(500) NOT NULL,
    query_string VARCHAR(1000),
    request_body TEXT,
    response_status INT,
    success BOOLEAN NOT NULL,
    error_message VARCHAR(1000),
    client_ip VARCHAR(64),
    duration_ms BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
COMMENT ON TABLE sysop_operation_log IS '后台管理接口（/api/v1/manage/**）非 GET 请求的操作留痕，拦截器自动写入';
CREATE INDEX idx_sysop_operation_log_operator ON sysop_operation_log(operator_id, created_at DESC);
CREATE INDEX idx_sysop_operation_log_path ON sysop_operation_log(request_path, created_at DESC);
