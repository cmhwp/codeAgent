package com.code.codeagent.model.dto.user;

import com.code.codeagent.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author CodeAgent
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "UserQueryRequest", description = "用户查询请求")
public class UserQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户账号")
    private String userAccount;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户邮箱")
    private String userEmail;

    @Schema(description = "用户角色：user/admin/ban")
    private String userRole;

    @Schema(description = "用户状态：0-正常，1-禁用")
    private Integer userStatus;

    @Schema(description = "搜索关键词（账号、昵称、邮箱）")
    private String searchText;
}