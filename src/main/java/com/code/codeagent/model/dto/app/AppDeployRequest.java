package com.code.codeagent.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 应用部署请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "AppDeployRequest", description = "应用部署请求")
public class AppDeployRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "应用ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "应用ID不能为空")
    private Long appId;
}