package cn.bugstack.chatgpt.data.infrastructure.dao;

import cn.bugstack.chatgpt.data.infrastructure.po.OpenAIPromptPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IOpenAIPromptDao {
    List<OpenAIPromptPO> queryPromptList();
}
