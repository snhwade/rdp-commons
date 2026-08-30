package com.riskplatform.common.crypto;

import javax.crypto.SecretKey;

/**
 * 数据密钥提供者抽象（R17.4 敏感数据加密）。
 *
 * <p>该接口把「敏感字段加密所用的数据密钥从何而来」与「如何加解密」解耦，
 * 是接入 KMS / Vault 的可替换扩展点：
 * <ul>
 *   <li><b>本地/开发环境</b>：由 {@link ConfigKeyProvider} 从配置或环境变量注入密钥种子；</li>
 *   <li><b>生产环境</b>：应实现新的 KeyProvider（如 {@code KmsKeyProvider}/{@code VaultKeyProvider}），
 *       通过云厂商 KMS 或 HashiCorp Vault 获取/解封数据密钥（信封加密），并替换 Spring 容器中的
 *       {@link KeyProvider} Bean 即可，无需改动加解密与持久化层代码。</li>
 * </ul>
 *
 * <p>实现须保证返回的密钥为 AES-256（32 字节）对称密钥。
 */
public interface KeyProvider {

    /**
     * 返回当前用于字段加解密的数据密钥（AES-256）。
     *
     * <p>实现可在内部做缓存与轮转；调用方不缓存返回值，以便密钥轮转即时生效。
     *
     * @return AES-256 对称密钥
     */
    SecretKey dataKey();

    /**
     * 返回当前数据密钥的标识（如 KMS keyId / Vault 密钥版本）。
     *
     * <p>用于审计与密钥轮转排查；本地实现可返回固定标识。
     *
     * @return 密钥标识
     */
    String keyId();
}
