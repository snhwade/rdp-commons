package com.riskplatform.common.crypto;

/**
 * 字段加解密器的静态持有者（R17.4）。
 *
 * <p>MyBatis 的 {@code TypeHandler} 由 MyBatis 自身实例化（不经 Spring 容器），
 * 无法直接注入 Spring 管理的 {@link AesGcmFieldCipher} Bean。为此提供该静态桥：
 * Spring 启动时由 {@link FieldCryptoConfig} 注入 cipher，
 * {@link EncryptedStringTypeHandler} 在读写时从此处取用。
 *
 * <p>该持有者仅写入一次（应用启动），运行期只读，线程安全。
 */
public final class FieldCryptoHolder {

    private static volatile AesGcmFieldCipher cipher;

    private FieldCryptoHolder() {
    }

    /** 由 Spring 配置在启动时注入（见 {@link FieldCryptoConfig}）。 */
    public static void setCipher(AesGcmFieldCipher cipher) {
        FieldCryptoHolder.cipher = cipher;
    }

    /**
     * 取用加解密器。
     *
     * @return 已装配的 {@link AesGcmFieldCipher}
     * @throws IllegalStateException 若未完成装配（通常表示缺少 {@link FieldCryptoConfig} 或扫描遗漏）
     */
    public static AesGcmFieldCipher cipher() {
        AesGcmFieldCipher c = cipher;
        if (c == null) {
            throw new IllegalStateException(
                    "字段加解密器未初始化：请确保已装配 FieldCryptoConfig（@Import 或组件扫描）");
        }
        return c;
    }
}
