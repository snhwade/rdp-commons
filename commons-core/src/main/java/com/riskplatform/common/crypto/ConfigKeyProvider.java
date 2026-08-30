package com.riskplatform.common.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 基于配置注入的数据密钥提供者（R17.4，本地/开发环境默认实现）。
 *
 * <p>密钥种子来源于配置项 {@code security.crypto.data-key}（推荐经环境变量
 * {@code SECURITY_CRYPTO_DATA_KEY} 注入），通过 SHA-256 派生出固定的 AES-256（32 字节）密钥。
 * 接受两种种子格式：
 * <ul>
 *   <li>Base64 编码的 32 字节原始密钥（优先按 Base64 解析，长度恰为 32 字节时直接使用）；</li>
 *   <li>任意口令字符串（对其 UTF-8 字节做 SHA-256 派生出 32 字节密钥）。</li>
 * </ul>
 *
 * <p><b>生产替换</b>：本实现仅用于本地可演示与集成验证。生产环境应以
 * {@code KmsKeyProvider}/{@code VaultKeyProvider} 替换本 Bean，由 KMS/Vault 托管主密钥与数据密钥，
 * 避免明文密钥进入应用配置（详见 {@link KeyProvider} 注释）。
 */
public class ConfigKeyProvider implements KeyProvider {

    /** AES 密钥长度（字节）：AES-256。 */
    private static final int AES_256_BYTES = 32;

    private final SecretKey key;
    private final String keyId;

    /**
     * @param dataKeySeed 配置注入的密钥种子（Base64 原始密钥或任意口令）
     * @param keyId       密钥标识（用于审计；本地可用配置项或固定值）
     */
    public ConfigKeyProvider(String dataKeySeed, String keyId) {
        if (dataKeySeed == null || dataKeySeed.isBlank()) {
            throw new IllegalArgumentException("数据密钥种子 security.crypto.data-key 不能为空");
        }
        this.key = new SecretKeySpec(deriveKeyBytes(dataKeySeed), "AES");
        this.keyId = keyId == null || keyId.isBlank() ? "local-config-key" : keyId;
    }

    @Override
    public SecretKey dataKey() {
        return key;
    }

    @Override
    public String keyId() {
        return keyId;
    }

    /** 将种子派生为 32 字节 AES-256 密钥：Base64 原始密钥优先，否则 SHA-256 派生。 */
    private static byte[] deriveKeyBytes(String seed) {
        byte[] decoded = tryDecodeBase64(seed);
        if (decoded != null && decoded.length == AES_256_BYTES) {
            return decoded;
        }
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(seed.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 为 JDK 标准算法，理论上不会发生
            throw new IllegalStateException("无法初始化 SHA-256 派生密钥", e);
        }
    }

    private static byte[] tryDecodeBase64(String s) {
        try {
            return Base64.getDecoder().decode(s);
        } catch (IllegalArgumentException notBase64) {
            return null;
        }
    }
}
