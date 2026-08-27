package com.mystikos.identity.infrastructure.crypto;

import java.security.interfaces.RSAPrivateKey;

/** 当前生效的登录加密密钥对，仅在本包内流转——私钥对象绝不会离开 infrastructure/crypto 包。 */
record LoginKeyMaterial(
        String keyId,
        String publicKeyPem,
        RSAPrivateKey privateKey,
        int modulusByteLength
) {

    /**
     * JDK 的 RSAPrivateKey 实现默认 toString() 会把模数/私有指数打印出来，
     * 这里必须覆盖掉，否则任何不小心的日志/断点打印都会把私钥明文写进日志。
     */
    @Override
    public String toString() {
        return "LoginKeyMaterial[keyId=" + keyId + ", modulusByteLength=" + modulusByteLength + "]";
    }
}
