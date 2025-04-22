package com.qingdai.entity.dto;

import com.qingdai.entity.GroupPhoto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GroupPhotoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public GroupPhoto groupPhoto;
    public List<String> photoIds;
}
    

