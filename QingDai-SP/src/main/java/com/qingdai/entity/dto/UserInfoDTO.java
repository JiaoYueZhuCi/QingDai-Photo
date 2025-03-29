package com.qingdai.entity.dto;

import com.qingdai.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class UserInfoDTO {
    private User user;
    private List<String> roles;
    private List<String> permissions;

    public UserInfoDTO(User user, List<String> roles, List<String> permissions) {
        this.user = user;
        this.roles = roles;
        this.permissions = permissions;
    }
}
