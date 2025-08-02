package com.code.codeagent.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 修改密码请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "ChangePasswordRequest", description = "修改密码请求")
public class ChangePasswordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "当前密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度应为8-20位")
    private String newPassword;

    @Schema(description = "确认新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}