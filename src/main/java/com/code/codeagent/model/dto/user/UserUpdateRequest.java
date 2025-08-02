package com.code.codeagent.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 管理员更新用户信息请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "UserUpdateRequest", description = "管理员更新用户信息请求")
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long id;

    @Schema(description = "用户昵称")
    @Size(max = 50, message = "用户昵称长度不能超过50字符")
    private String userName;

    @Schema(description = "用户头像URL")
    @Size(max = 500, message = "头像URL长度不能超过500字符")
    private String userAvatar;

    @Schema(description = "用户简介")
    @Size(max = 200, message = "用户简介长度不能超过200字符")
    private String userProfile;

    @Schema(description = "用户邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100字符")
    private String userEmail;

    @Schema(description = "用户角色")
    private String userRole;

    @Schema(description = "用户状态")
    private Integer userStatus;
}