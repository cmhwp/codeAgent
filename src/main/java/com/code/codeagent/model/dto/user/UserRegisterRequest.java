package com.code.codeagent.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户注册请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "UserRegisterRequest", description = "用户注册请求")
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120794L;

    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 16, message = "账号长度应为4-16位")
    private String userAccount;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度应为8-20位")
    private String userPassword;

    @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String checkPassword;

    @Schema(description = "用户昵称（可选）")
    @Size(max = 50, message = "用户昵称长度不能超过50字符")
    private String userName;

    @Schema(description = "用户邮箱（可选）")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100字符")
    private String userEmail;
}