-- 登录公钥接口不需要登录：前端凭这个公钥用 RSA-OAEP-256 加密登录密码，见
-- com.mystikos.identity.infrastructure.crypto.LoginKeyProvider / AuthController#publicKey。
INSERT INTO security_whitelist_path (id, path_pattern, http_method, description, enabled) VALUES
    (9, '/api/v1/auth/public-key', 'GET', '获取登录加密公钥', true);
