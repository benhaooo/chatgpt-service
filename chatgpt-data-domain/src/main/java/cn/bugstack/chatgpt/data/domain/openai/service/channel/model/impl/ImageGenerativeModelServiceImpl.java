package cn.bugstack.chatgpt.data.domain.openai.service.channel.model.impl;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.MessageEntity;
import cn.bugstack.chatgpt.data.domain.openai.service.channel.model.IGenerativeModelService;

import cn.bugstack.openai.executor.parameter.*;
import cn.bugstack.openai.session.OpenAiSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description 图片生成
 */
@Slf4j
@Service
public class ImageGenerativeModelServiceImpl implements IGenerativeModelService {

    @Autowired(required = false)
    protected OpenAiSession chatGPTOpenAiSession;

    @Resource
    private ThreadPoolExecutor executor;

    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) throws Exception {
        if (null == chatGPTOpenAiSession) {
            emitter.send("DALL-E 通道，模型调用未开启，可以选择其他模型对话！");
            return;
        }

        // 封装请求信息
        StringBuilder prompt = new StringBuilder();
        List<MessageEntity> messages = chatProcess.getMessages();
        for (MessageEntity message : messages) {
            String role = message.getRole();
            if (CompletionRequest.Role.USER.getCode().equals(role)) {
                prompt.append(message.getContent());
                prompt.append("\r\n");
            }
        }

        // 绘图请求信息
        ImageRequest request = ImageRequest.builder()
                .prompt(prompt.toString())
                .model(chatProcess.getModel())
                .size(ImageEnum.Size.size_1024.getCode())
                .build();

//        emitter.send("您的😊图片正在生成中，请耐心等待... \r\n");

        executor.execute(() -> {
            ImageResponse imageResponse = null;
            try {
                imageResponse = chatGPTOpenAiSession.genImages(request);
                List<Item> items = imageResponse.getData();

                for (Item item : items) {
                    String revised_prompt = item.getRevisedPrompt();
                    emitter.send("您的😊图片生成成功，说明：" + revised_prompt + " \r\n");
                    String url = item.getUrl();
                    emitter.send("![](" + url + ")");
                }
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send("您的😭图片生成失败了，请调整说明... \r\n");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

    }

}
