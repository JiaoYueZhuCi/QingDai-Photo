package ${package.Entity};

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
<#list table.importPackages as pkg>
import ${pkg};
</#list>

@Data
@TableName("${table.name}")
@Schema(name = "${entity}实体")
public class ${entity} {
<#list table.fields as field>
    <#if field.keyFlag>
    @TableId(value = "${field.annotationColumnName}", type = IdType.<#if field.keyIdentityFlag>AUTO<#elseif field.convert>ASSIGN_ID<#else>NONE</#if>)
    <#else>
    @TableField(value = "${field.annotationColumnName}")
    </#if>
    @Schema(description = "${field.comment!}")
    private ${field.propertyType} ${field.propertyName};
</#list>
}