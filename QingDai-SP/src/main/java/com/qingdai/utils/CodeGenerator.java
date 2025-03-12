package com.qingdai.utils;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.sql.Types;
import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/qingdai-photo?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8";

        FastAutoGenerator.create(url, "root", "123456")
                .globalConfig(builder -> {
                    builder.author("LiuZiMing")
                            .enableSwagger() // 启用Swagger3注解
                            .outputDir("D:/Code/qingdai-photo/QingDai-SP/src/main/java");
                })
                .dataSourceConfig(builder -> builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                    int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                    if (typeCode == Types.SMALLINT) {
                        return DbColumnType.INTEGER;
                    }
                    return typeRegistry.getColumnType(metaInfo);
                }))
                .packageConfig(builder -> builder.parent("com.qingdai") // 直接指定父包名
                        .pathInfo(Collections.singletonMap(OutputFile.xml,
                                "D:/Code/qingdai-photo/QingDai-SP/src/main/resources/mapper"))
                )
                .strategyConfig(builder -> {
                    builder.addInclude("sys_user_role")
                            .entityBuilder()
                            .enableLombok()
//                            .enableFileOverride()  //!!!
                            .idType(IdType.ASSIGN_ID) // 新增主键策略配置
                            .addTableFills() // 可选字段填充配置
                            .versionColumnName("version") // 乐观锁字段
                            .enableTableFieldAnnotation() // 启用字段注解
//                            .formatFileName("%s")  //去前缀
                            .enableActiveRecord()
                        
                            .controllerBuilder()
                            .enableRestStyle()
//                        .enableFileOverride()  //!!!
                            .mapperBuilder()
                            .enableMapperAnnotation()
                            .enableBaseResultMap()
//                        .enableFileOverride() //!!!
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")
//                        .enableFileOverride()  //!!!
                    ;
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker模板引擎
                .templateConfig(builder ->
                        builder.entity("/templates/entity.java") // 自定义实体模板
                )
                .execute();
    }
}