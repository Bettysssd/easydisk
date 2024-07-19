package com.easypan.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionWebUserDto {
    private String nickName;
    private String userId;
    private Boolean isAdmin;
    // 头像
    private String avatar;


}
