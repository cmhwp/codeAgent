package com.code.codeagent.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 应用更新请求（用户）
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "AppUpdateRequest", description = "应用更新请求（用户）")
public class AppUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应用ID不能为空")
    private Long id;

    @Schema(description = "应用名称")
    @Size(max = 100, message = "应用名称长度不能超过100字符")
    private String appName;

    @Schema(description = "应用封面URL")
    @Size(max = 500, message = "应用封面URL长度不能超过500字符")
    private String cover;
}