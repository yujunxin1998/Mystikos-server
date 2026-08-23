-- Public endpoints used by the server-managed OAuth authorization-code flow.
INSERT INTO security_whitelist_path (id, path_pattern, http_method, description, enabled) VALUES
    (6, '/api/v1/auth/oauth/*/authorize', 'GET', '发起第三方 OAuth 登录', true),
    (7, '/api/v1/auth/oauth/*/callback', 'GET', '第三方 OAuth 回调', true),
    (8, '/api/v1/auth/oauth/tickets', 'POST', '兑换一次性 OAuth 登录票据', true);
