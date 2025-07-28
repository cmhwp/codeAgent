package com.code.codeagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author CodeAgent
 * @since 2024-12-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
@Schema(name = "User", description = "用户")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "账号")
    @TableField("userAccount")
    private String userAccount;

    @Schema(description = "密码")
    @TableField("userPassword")
    private String userPassword;

    @Schema(description = "用户昵称")
    @TableField("userName")
    private String userName;

    @Schema(description = "用户头像")
    @TableField("userAvatar")
    private String userAvatar;

    @Schema(description = "用户简介")
    @TableField("userProfile")
    private String userProfile;

    @Schema(description = "用户邮箱")
    @TableField("userEmail")
    private String userEmail;

    @Schema(description = "用户角色：user/admin")
    @TableField("userRole")
    private String userRole;

    @Schema(description = "用户状态：0-正常，1-禁用")
    @TableField("userStatus")
    private Integer userStatus;

    @Schema(description = "编辑时间")
    @TableField("editTime")
    private LocalDateTime editTime;

    @Schema(description = "创建时间")
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "最后登录时间")
    @TableField("lastLoginTime")
    private LocalDateTime lastLoginTime;

    @Schema(description = "是否删除")
    @TableField("isDelete")
    @TableLogic
    private Integer isDelete;
}
