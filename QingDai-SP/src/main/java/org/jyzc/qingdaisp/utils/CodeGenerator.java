package org.jyzc.qingdaisp.utils;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

// 其他配置类...
public class CodeGenerator {
    public static void main(String[] args) {
        AutoGenerator generator = new AutoGenerator();

        // 1. 全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOutputDir(System.getProperty("user.dir") + "/src/main/java");
        globalConfig.setAuthor("YourName");
        globalConfig.setOpen(false); // 生成后不打开文件夹
        globalConfig.setFileOverride(true); // 覆盖已有文件
        globalConfig.setServiceName("%sService"); // Service 接口命名规则
        generator.setGlobalConfig(globalConfig);

        // 2. 数据源配置
        DataSourceConfig dataSource = new DataSourceConfig();
        dataSource.setUrl("jdbc:mysql://localhost:3306/your_db");
        dataSource.setDriverName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        generator.setDataSource(dataSource);

        // 3. 包路径配置
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent("com.example"); // 父包名
        packageConfig.setEntity("entity");      // 实体类包名
        packageConfig.setMapper("mapper");      // Mapper 接口包名
        packageConfig.setService("service");    // Service 包名
        packageConfig.setController("controller");
        generator.setPackageInfo(packageConfig);

        // 4. 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel); // 表名转驼峰
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setEntityLombokModel(true);    // 使用 Lombok
        strategy.setInclude("user", "order");   // 指定生成表名
        strategy.setRestControllerStyle(true);  // RestController
        generator.setStrategy(strategy);

        // 5. 模板配置（可选）
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setController("templates/controller.java"); // 自定义模板路径
        generator.setTemplate(templateConfig);

        // 执行生成
        generator.execute();
    }
}