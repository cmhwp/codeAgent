package com.code.codeagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.code.codeagent.model.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话历史 Mapper 接口
 *
 * @author CodeAgent
 */
@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

}