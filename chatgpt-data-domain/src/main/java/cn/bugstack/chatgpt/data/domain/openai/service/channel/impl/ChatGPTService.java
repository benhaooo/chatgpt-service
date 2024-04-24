package cn.bugstack.chatgpt.data.domain.openai.service.channel.impl;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.GenerativeModelVO;
import cn.bugstack.chatgpt.data.domain.openai.service.channel.OpenAiGroupService;
import cn.bugstack.chatgpt.data.domain.openai.service.channel.model.IGenerativeModelService;
import cn.bugstack.chatgpt.data.domain.openai.service.channel.model.impl.ImageGenerativeModelServiceImpl;
import cn.bugstack.chatgpt.data.domain.openai.service.channel.model.impl.TextGenerativeModelServiceImpl;
import cn.bugstack.chatgpt.session.OpenAiSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @description ChatGPT 服务
 */
@Service
public class ChatGPTService implements OpenAiGroupService {

    @Autowired(required = false)
    protected OpenAiSession chatGPTOpenAiSession;

    private final Map<GenerativeModelVO, IGenerativeModelService> generativeModelGroup = new HashMap<>();

    public ChatGPTService(ImageGenerativeModelServiceImpl imageGenerativeModelService, TextGenerativeModelServiceImpl textGenerativeModelService) {
        generativeModelGroup.put(GenerativeModelVO.IMAGES, imageGenerativeModelService);
        generativeModelGroup.put(GenerativeModelVO.TEXT, textGenerativeModelService);
    }

    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws IOException {
        GenerativeModelVO generativeModelVO = chatProcess.getGenerativeModelVO();
        generativeModelGroup.get(generativeModelVO).doMessageResponse(chatProcess, emitter);
    }

}
