package cn.bugstack.chatgpt.data.domain.openai.service.channel.model.impl;


import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.service.channel.model.IGenerativeModelService;
//import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
//import cn.bugstack.openai.executor.parameter.ChatChoice;
//import cn.bugstack.openai.executor.parameter.CompletionRequest;
//import cn.bugstack.openai.executor.parameter.CompletionResponse;
//import cn.bugstack.openai.session.OpenAiSession;
import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
import com.alibaba.fastjson.JSON;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.*;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import com.unfbx.chatgpt.sse.ConsoleEventSourceListener;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @description 文本生成
 */
@Slf4j
@Service
public class TextGenerativeModelServiceImpl implements IGenerativeModelService {

//    @Autowired(required = false)
//    private OpenAiSession openAiSession;

    @Override
    public void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter emitter) {

        if (StringUtils.isNoneBlank(chatProcess.getMessages().get(chatProcess.getMessages().size() - 1).getImg())) {
            OkHttpClient okHttpClient = new OkHttpClient
                    .Builder()
                    .addInterceptor(new OpenAiResponseInterceptor())
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            OpenAiClient clientP = OpenAiClient.builder()
                    .okHttpClient(okHttpClient)
                    .apiKey(Arrays.asList("hk-jnirdw1000003982242afd989f4c0da72f772011a587517f"))
                    .apiHost("https://api.openai-hk.com/")
                    .build();
//            ConsoleEventSourceListener eventSourceListener = new ConsoleEventSourceListener();

            List<MessagePicture> messages = chatProcess.getMessages().stream()
                    .map(entity -> {
                        List<Content> contentList = new ArrayList<>();
                        Content textContent = Content.builder().text(entity.getContent()).type(Content.Type.TEXT.getName()).build();
                        contentList.add(textContent);
                        if (StringUtils.isNoneBlank(entity.getImg())) {
                            ImageUrl imageUrl = ImageUrl.builder().url(entity.getImg()).build();
                            Content imageContent = Content.builder().imageUrl(imageUrl).type(Content.Type.IMAGE_URL.getName()).build();
                            contentList.add(imageContent);
                        }
                        MessagePicture message = MessagePicture.builder().role(Message.Role.valueOf(entity.getRole().toUpperCase())).content(contentList).build();
                        return message;
                    })
                    .collect(Collectors.toList());
            ChatCompletionWithPicture chatCompletion = ChatCompletionWithPicture
                    .builder()
                    .messages(messages)
                    .model(ChatCompletion.Model.GPT_4_VISION_PREVIEW.getName())
                    .build();
            ChatCompletionResponse chatCompletionResponse = clientP.chatCompletion(chatCompletion);
            System.out.println(chatCompletionResponse);
            chatCompletionResponse.getChoices().forEach(choice -> {
                try {
                    String content = choice.getMessage().getContent();
                    emitter.send(content);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            emitter.complete();

        } else {
            OpenAiStreamClient client = OpenAiStreamClient.builder()
                    .apiKey(Arrays.asList("hk-jnirdw1000003982242afd989f4c0da72f772011a587517f"))
                    .apiHost("https://api.openai-hk.com/")
                    .build();

            List<Message> messages = chatProcess.getMessages().stream()
                    .map(entity -> Message.builder()
                            .role(Message.Role.valueOf(entity.getRole().toUpperCase()))
                            .content(entity.getContent())
                            .name(entity.getName())
                            .build())
                    .collect(Collectors.toList());
            ChatCompletion chatCompletion = ChatCompletion.builder().messages(messages)
                    .build();
            client.streamChatCompletion(chatCompletion, new EventSourceListener() {
                @Override
                public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                    ChatCompletionResponse chatCompletionResponse = JSON.parseObject(data, ChatCompletionResponse.class);
                    List<ChatChoice> choices = chatCompletionResponse.getChoices();
                    for (ChatChoice chatChoice : choices) {
                        Message delta = chatChoice.getDelta();
                        if (Message.Role.ASSISTANT.getName().equals(delta.getRole())) continue;

                        // 应答完成
                        String finishReason = chatChoice.getFinishReason();
                        if (StringUtils.isNoneBlank(finishReason) && "stop".equals(finishReason)) {
                            emitter.complete();
                            break;
                        }

                        // 发送信息
                        try {
                            emitter.send(delta.getContent());
                        } catch (Exception e) {
                            throw new ChatGPTException(e.getMessage());
                        }
                    }
                }
            });
        }


    }


    public CompletableFuture<String> doMessageResponse(ChatProcessAggregate chatProcess) throws Exception {
//        List<Message> messages = chatProcess.getMessages().stream()
//                .map(entity -> Message.builder()
//                        .role(CompletionRequest.Role.valueOf(entity.getRole().toUpperCase()))
//                        .content(entity.getContent())
//                        .name(entity.getName())
//                        .build())
//                .collect(Collectors.toList());
//
//        // 2. 封装参数
//        CompletionRequest chatCompletion = CompletionRequest
//                .builder()
//                .stream(true)
//                .messages(messages)
//                .model(chatProcess.getModel())
//                .build();
//
//        CompletableFuture<String> completions = openAiSession.completions(chatCompletion);
//        return completions;
        return null;
    }
}
