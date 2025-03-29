package com.qingdai.entity.dto;

import com.qingdai.entity.GroupPhoto;
import lombok.Data;

import java.util.List;

@Data
public class GroupPhotoDTO {
    public GroupPhoto groupPhoto;
    public List<String> photoIds;
}
    

