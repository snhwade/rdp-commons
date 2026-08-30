package com.riskplatform.common.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 字段加解密器（R17.4 敏感数据落库加密）。
 *
 * <p>仅依赖 JDK 自带的 {@code javax.crypto}（全开源、无第三方加密库），对单个字段值进行
 * 透明加解密。GCM 提供机密性与完整性（带认证标签），是对称认证加密的推荐模式。
 *
 * <p><b>密文封装格式</b>（落库为字符串，便于存入既有 TEXT/VARCHAR 列）：
 * <pre>
 *   "ENC1:" + Base64( IV(12B) || ciphertext+tag )
 * </pre>
 * 其中：
 * <ul>
 *   <li>{@code ENC1:} 版本前缀，便于识别密文与未来算法升级；</li>
 *   <li>每次加密随机生成 12 字节 IV（GCM 推荐长度），与密文一起存储；</li>
 *   <li>GCM 认证标签 128 位附于密文尾部（由 JDK Cipher 自动追加）。</li>
 * </ul>
 *
 * <p>数据密钥来自可替换的 {@link KeyProvider}（本地配置 / 生产 KMS-Vault），本类不持有静态密钥。
 */
public class AesGcmFieldCipher {

    /** 密文版本前缀，用于识别与未来算法演进。 */
    public static final String PREFIX = "ENC1:";

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;          // GCM 推荐 96 位 IV
    private static final int TAG_LENGTH_BITS = 128;   // GCM 认证标签 128 位

    private final KeyProvider keyProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmFieldCipher(KeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    /**
     * 加密明文字段值，返回带前缀的 Base64 密文字符串。
     *
     * <p>{@code null} 原样返回 {@code null}；空串照常加密（便于区分 null 与空）。
     * 若入参已是本类密文（带 {@link #PREFIX}），直接返回，避免重复加密。
     *
     * @param plaintext 明文（可为 null）
     * @return 密文字符串（{@code ENC1:Base64...}），或 null
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        if (isEncrypted(plaintext)) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keyProvider.dataKey(),
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] packed = ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(iv)
                    .put(cipherText)
                    .array();
            return PREFIX + Base64.getEncoder().encodeToString(packed);
        } catch (Exception e) {
            throw new FieldCryptoException("敏感字段加密失败", e);
        }
    }

    /**
     * 解密密文字段值，返回明文。
     *
     * <p>{@code null} 原样返回 {@code null}；若入参不是本类密文（无 {@link #PREFIX}），
     * 视为历史明文数据，原样返回以兼容存量数据。
     *
     * @param stored 落库值（密文或历史明文，可为 null）
     * @return 明文，或 null
     */
    public String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        if (!isEncrypted(stored)) {
            // 兼容加密上线前写入的历史明文数据
            return stored;
        }
        try {
            byte[] packed = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(packed);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keyProvider.dataKey(),
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new FieldCryptoException("敏感字段解密失败", e);
        }
    }

    /** 是否为本类生成的密文（带版本前缀）。 */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    /** 加解密异常：包裹底层 {@link javax.crypto} 异常，避免泄漏密钥/算法细节到上层。 */
    public static class FieldCryptoException extends RuntimeException {
        public FieldCryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
