package com.riskplatform.common.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 敏感字段加密装配（R17.4）。
 *
 * <p>各需要落库加密的服务（如 decision-gateway、screening-service）以
 * {@code @Import(FieldCryptoConfig.class)} 引入即可获得：
 * <ul>
 *   <li>{@link KeyProvider}：数据密钥来源抽象，默认 {@link ConfigKeyProvider}（配置/环境变量注入），
 *       生产可声明同名 Bean 覆盖为 KMS/Vault 实现（{@code @ConditionalOnMissingBean} 允许替换）；</li>
 *   <li>{@link AesGcmFieldCipher}：AES-256-GCM 加解密器；</li>
 *   <li>启动时把 cipher 注入 {@link FieldCryptoHolder}，供 MyBatis {@link EncryptedStringTypeHandler} 取用。</li>
 * </ul>
 *
 * <p>配置项：
 * <ul>
 *   <li>{@code security.crypto.data-key}（建议经环境变量 {@code SECURITY_CRYPTO_DATA_KEY} 注入）：
 *       数据密钥种子，本地默认仅供开发演示；</li>
 *   <li>{@code security.crypto.key-id}：密钥标识，用于审计与轮转排查。</li>
 * </ul>
 */
@Configuration
public class FieldCryptoConfig {

    /**
     * 默认数据密钥提供者：从配置注入密钥种子。
     *
     * <p>生产环境实现 KMS/Vault 版 {@link KeyProvider} 并声明为 Bean 即可自动覆盖本默认实现。
     */
    @Bean
    @ConditionalOnMissingBean(KeyProvider.class)
    public KeyProvider keyProvider(
            @Value("${security.crypto.data-key:rdp-local-dev-data-key-change-in-production}") String dataKeySeed,
            @Value("${security.crypto.key-id:local-config-key}") String keyId) {
        return new ConfigKeyProvider(dataKeySeed, keyId);
    }

    @Bean
    public AesGcmFieldCipher aesGcmFieldCipher(KeyProvider keyProvider) {
        AesGcmFieldCipher cipher = new AesGcmFieldCipher(keyProvider);
        // 将加解密器暴露给 MyBatis TypeHandler（TypeHandler 不经 Spring 实例化）
        FieldCryptoHolder.setCipher(cipher);
        return cipher;
    }
}
