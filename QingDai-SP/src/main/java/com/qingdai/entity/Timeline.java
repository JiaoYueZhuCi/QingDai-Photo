package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@TableName("timeline")
@Schema(name = "Timeline实体")
public class Timeline {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;
    @TableField(value = "time")
    @Schema(description = "时间")
    private String time;
    @TableField(value = "title")
    @Schema(description = "标题")
    private String title;
    @TableField(value = "text")
    @Schema(description = "正文")
    private String text;
}