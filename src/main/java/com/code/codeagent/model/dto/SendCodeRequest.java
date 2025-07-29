package com.code.codeagent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 发送验证码请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "SendCodeRequest", description = "发送验证码请求")
public class SendCodeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "验证码用途：bind_email-邮箱绑定，reset_password-重置密码，change_email-修改邮箱", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码用途不能为空")
    private String purpose;
} 