package com.code.codeagent.mapper;

import com.code.codeagent.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 *
 * @author CodeAgent
 * @since 2024-12-19
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
