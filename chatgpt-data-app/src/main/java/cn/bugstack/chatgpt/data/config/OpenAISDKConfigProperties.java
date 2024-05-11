package cn.bugstack.chatgpt.data.config;

import cn.bugstack.openai.executor.model.chatglm.config.ChatGLMConfig;
import cn.bugstack.openai.executor.model.chatgpt.config.ChatGPTConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "openai.sdk.config", ignoreInvalidFields = true)
public class OpenAISDKConfigProperties {
    // ChatGPT
    private ChatGPTConfig chatGPTConfig;
}
