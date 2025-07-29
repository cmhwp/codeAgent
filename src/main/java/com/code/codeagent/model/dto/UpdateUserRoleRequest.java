package com.code.codeagent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 更新用户角色请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "UpdateUserRoleRequest", description = "更新用户角色请求")
public class UpdateUserRoleRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "新角色：user-普通用户，admin-管理员，ban-被封号", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色不能为空")
    private String newRole;
} 