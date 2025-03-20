package com.qingdai.dto;

import com.qingdai.entity.GroupPhoto;
import com.qingdai.entity.Photo;
import lombok.Data;

@Data
public class GroupPhotoDTO {
    public GroupPhoto groupPhoto;
    public Photo photo;
}