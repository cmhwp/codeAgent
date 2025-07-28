package com.code.codeagent.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 已登录用户视图对象
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "LoginUserVO", description = "已登录用户信息")
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "账号")
    private String userAccount;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户头像")
    private String userAvatar;

    @Schema(description = "用户简介")
    private String userProfile;

    @Schema(description = "用户邮箱")
    private String userEmail;

    @Schema(description = "用户角色：user/admin")
    private String userRole;

    @Schema(description = "用户状态：0-正常，1-禁用")
    private Integer userStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "访问token")
    private String token;
} 