package org.example.springboot3java21demo.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.Date;

public class MddLong2DateTypeHandler extends BaseTypeHandler<Date> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Date date, JdbcType jdbcType) throws SQLException {
        preparedStatement.setLong(i, date.getTime());
    }

    @Override
    public Date getNullableResult(ResultSet resultSet, String s) throws SQLException {
        if (resultSet != null) {
            try {
                if (resultSet.getLong(s) > 0L) {
                    return new Date(resultSet.getLong(s));
                }
            } catch (Exception e) {
                Timestamp timestamp = resultSet.getTimestamp(s);
                if (timestamp != null) {
                    return new Date(timestamp.getTime());
                }
            }
        }
        return null;
    }

    @Override
    public Date getNullableResult(ResultSet resultSet, int i) throws SQLException {
        if (resultSet != null) {
            try {
                if (resultSet.getLong(i) > 0L) {
                    return new Date(resultSet.getLong(i));
                }
            } catch (Exception e) {
                Timestamp timestamp = resultSet.getTimestamp(i);
                if (timestamp != null) {
                    return new Date(timestamp.getTime());
                }
            }
        }
        return null;
    }

    @Override
    public Date getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        if (callableStatement != null) {
            try {
                if (callableStatement.getLong(i) > 0L) {
                    return new Date(callableStatement.getLong(i));
                }
            } catch (Exception e) {
                Timestamp timestamp = callableStatement.getTimestamp(i);
                if (timestamp != null) {
                    return new Date(timestamp.getTime());
                }
            }
        }
        return null;
    }
}

