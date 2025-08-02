package com.code.codeagent.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 批量用户操作请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "BatchUserOperationRequest", description = "批量用户操作请求")
public class BatchUserOperationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "用户ID列表不能为空")
    private List<Long> userIds;

    @Schema(description = "操作类型：delete-删除，ban-封禁，unban-解封，role-修改角色", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作类型不能为空")
    private String operation;

    @Schema(description = "操作参数（如角色类型）")
    private String parameter;
}