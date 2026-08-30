package com.riskplatform.common.crypto;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 敏感字符串字段透明加解密 TypeHandler（R17.4）。
 *
 * <p>在 MyBatis 落库/读取时对 {@link String} 字段自动加解密：
 * <ul>
 *   <li>写库：将明文经 {@link AesGcmFieldCipher#encrypt(String)} 转为 {@code ENC1:Base64} 密文落库；</li>
 *   <li>读库：将密文经 {@link AesGcmFieldCipher#decrypt(String)} 还原为明文返回应用层。</li>
 * </ul>
 *
 * <p>在 PO 字段上以 {@code @TableField(typeHandler = EncryptedStringTypeHandler.class)} 启用，
 * 并在实体 {@code @TableName(autoResultMap = true)}。加解密器经 {@link FieldCryptoHolder} 取用。
 */
@MappedTypes(String.class)
public class EncryptedStringTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, FieldCryptoHolder.cipher().encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return FieldCryptoHolder.cipher().decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return FieldCryptoHolder.cipher().decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return FieldCryptoHolder.cipher().decrypt(cs.getString(columnIndex));
    }
}
