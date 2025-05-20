package com.qingdai.config;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * MyBatis SQL日志拦截器
 * 根据DynamicLogConfig的开关状态来控制是否输出完整SQL日志
 */
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Component
public class LogInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String SQL_BORDER = "┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐";
    private static final String SQL_BORDER_END = "└─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 只有在启用SQL日志的情况下才执行日志处理
        if (!DynamicLogConfig.isSqlLoggingEnabled()) {
            return invocation.proceed();
        }

        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        
        // 获取SQL语句
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        String sqlId = mappedStatement.getId(); // 获取Mapper方法的完整路径
        String shortSqlId = sqlId.substring(sqlId.lastIndexOf('.') + 1); // 获取简短的方法名
        
        // 获取SQL命令类型
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        
        // 获取完整的SQL语句
        String fullSql = getSql(configuration, boundSql, sqlId);
        
        // 记录执行开始时间
        long startTime = System.currentTimeMillis();
        String startTimeStr = DATE_FORMAT.format(new Date(startTime));
        
        Object result = invocation.proceed();
        
        // 记录执行结束时间
        long endTime = System.currentTimeMillis();
        String endTimeStr = DATE_FORMAT.format(new Date(endTime));
        long costTime = endTime - startTime;
        
        // 获取线程信息
        String threadInfo = Thread.currentThread().getName() + ":" + Thread.currentThread().getId();
        
        // 构建日志信息
        StringBuilder sqlInfo = new StringBuilder();
        sqlInfo.append(LINE_SEPARATOR).append(SQL_BORDER);
        sqlInfo.append(LINE_SEPARATOR).append("│ 线程信息: ").append(threadInfo);
        sqlInfo.append(LINE_SEPARATOR).append("│ SQL类型: ").append(sqlCommandType.name());
        sqlInfo.append(LINE_SEPARATOR).append("│ 完整路径: ").append(sqlId);
        sqlInfo.append(LINE_SEPARATOR).append("│ 方法名称: ").append(shortSqlId);
        sqlInfo.append(LINE_SEPARATOR).append("│ 开始时间: ").append(startTimeStr);
        sqlInfo.append(LINE_SEPARATOR).append("│ 结束时间: ").append(endTimeStr);
        sqlInfo.append(LINE_SEPARATOR).append("│ 执行耗时: ").append(costTime).append("ms");
        
        // 对于更新操作，尝试获取影响的行数
        if (result instanceof Integer && sqlCommandType != SqlCommandType.SELECT) {
            sqlInfo.append(LINE_SEPARATOR).append("│ 影响行数: ").append(result);
        }
        
        sqlInfo.append(LINE_SEPARATOR).append("│ 原始参数: ").append(parameter);
        sqlInfo.append(LINE_SEPARATOR).append("│ 完整SQL: ").append(fullSql);
        sqlInfo.append(LINE_SEPARATOR).append(SQL_BORDER_END);
        
        // 根据执行时间输出不同级别的日志
        if (costTime > 1000) {
            log.warn(sqlInfo.toString()); // 超过1秒使用警告级别
        } else if (costTime > 300) {
            log.warn(sqlInfo.toString()); // 超过300ms使用警告级别
        } else {
            log.info(sqlInfo.toString()); // 其他情况使用信息级别
        }
        
        return result;
    }

    /**
     * 获取完整的SQL语句，替换所有的问号为实际参数值
     */
    private String getSql(Configuration configuration, BoundSql boundSql, String sqlId) {
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Object parameterObject = boundSql.getParameterObject();
        
        // 如果没有参数，直接返回SQL
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return sql;
        }
        
        // 替换所有参数
        StringBuilder paramInfoBuilder = new StringBuilder();
        int paramIndex = 0;
        
        for (ParameterMapping parameterMapping : parameterMappings) {
            String propertyName = parameterMapping.getProperty();
            Object parameterValue;
            
            // 获取参数值
            if (boundSql.hasAdditionalParameter(propertyName)) {
                parameterValue = boundSql.getAdditionalParameter(propertyName);
            } else if (parameterObject == null) {
                parameterValue = null;
            } else if (configuration.getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass())) {
                parameterValue = parameterObject;
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                parameterValue = metaObject.hasGetter(propertyName) ? metaObject.getValue(propertyName) : null;
            }
            
            // 将参数转换为字符串并替换第一个问号
            String paramValueStr = getParameterValue(parameterValue);
            sql = sql.replaceFirst("\\?", paramValueStr);
            
            // 收集参数信息
            paramInfoBuilder.append(LINE_SEPARATOR).append("│   参数[").append(paramIndex++).append("]: ")
                           .append(propertyName).append(" = ").append(paramValueStr);
        }
        
        // 如果有参数，添加参数详情
        if (paramIndex > 0) {
            sql = sql + paramInfoBuilder.toString();
        }
        
        return sql;
    }

    /**
     * 将参数转换为字符串
     */
    private String getParameterValue(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            return "'" + obj.toString().replace("'", "''") + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            return "'" + formatter.format(obj) + "'";
        } else {
            return obj.toString();
        }
    }

    @Override
    public Object plugin(Object target) {
        // 当目标类是Executor类型时，才包装目标类，否则直接返回目标本身
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        // 获取配置参数
    }
} 