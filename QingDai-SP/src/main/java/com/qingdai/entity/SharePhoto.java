package com.qingdai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("share_photo")
public class SharePhoto {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String shareId;
    
    private String photoIds;
    
    private LocalDateTime createTime;
    
    private LocalDateTime expireTime;
    
    private Boolean isValid;
} 