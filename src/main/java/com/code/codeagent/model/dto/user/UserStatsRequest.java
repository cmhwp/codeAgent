package com.code.codeagent.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户统计请求
 *
 * @author CodeAgent
 */
@Data
@Schema(name = "UserStatsRequest", description = "用户统计请求")
public class UserStatsRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "统计开始日期")
    private LocalDate startDate;

    @Schema(description = "统计结束日期")
    private LocalDate endDate;

    @Schema(description = "统计类型：daily-按日统计，monthly-按月统计，yearly-按年统计")
    private String statsType;

    @Schema(description = "用户角色筛选")
    private String userRole;
}