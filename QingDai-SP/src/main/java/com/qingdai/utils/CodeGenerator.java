package com.qingdai.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;

import java.sql.Types;
import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/qingdai-photo?useUnicode=true&characterEncoding=utf-8&serverTimezoneGMT%2B8";

        FastAutoGenerator.create(url, "root", "123456")
                .globalConfig(builder -> {
                    builder.author("LiuZiMing")
                            .enableSwagger()
                            .outputDir("D:/Code/qingdai-photo/QingDai-SP/src/main/java");
                })
                .dataSourceConfig(builder ->
                        builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.SMALLINT) {
                                return DbColumnType.INTEGER; // 将SMALLINT转为Integer
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        })
                )
                .packageConfig(builder ->
                        builder.parent("com")
                                .moduleName("qingdai")
                                .pathInfo(Collections.singletonMap(OutputFile.xml, "D:/Code/qingdai-photo/QingDai-SP/src/main/resources/mapper"))
                )
                .strategyConfig(builder -> {
                    builder.addInclude("photo")
                            .entityBuilder()
                            .enableLombok()
//                               .enableFileOverride()
                            .mapperBuilder()
                            .enableMapperAnnotation()
//                               .enableFileOverride()
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")
//                               .enableFileOverride()
                            .controllerBuilder()
                            .enableRestStyle()
//                               .enableFileOverride()
                    ;
                })
                .execute();
    }
}