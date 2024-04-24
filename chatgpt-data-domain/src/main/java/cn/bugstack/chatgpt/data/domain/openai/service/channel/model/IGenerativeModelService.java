package cn.bugstack.chatgpt.data.domain.openai.service.channel.model;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;

/**
 * @description 模型生成文字/图片接口服务
 */
public interface IGenerativeModelService {

    void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws IOException;

}
