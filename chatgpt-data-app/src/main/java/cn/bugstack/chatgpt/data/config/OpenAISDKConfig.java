package cn.bugstack.chatgpt.data.config;

import cn.bugstack.openai.session.OpenAiSession;
import cn.bugstack.openai.session.OpenAiSessionFactory;
import cn.bugstack.openai.session.defaults.DefaultOpenAiSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpenAISDKConfigProperties.class)
public class OpenAISDKConfig {

    @Bean
    @ConditionalOnProperty(value = "openai.sdk.config.enabled", havingValue = "true", matchIfMissing = false)
    public OpenAiSession openAiSession(OpenAISDKConfigProperties properties) {
        // 1. 配置文件
        cn.bugstack.openai.session.Configuration configuration = new cn.bugstack.openai.session.Configuration();
        // 添加配置
        configuration.setChatGPTConfig(properties.getChatGPTConfig());
        // 2. 会话工厂
        OpenAiSessionFactory factory = new DefaultOpenAiSessionFactory(configuration);

        // 3. 开启会话
        return factory.openSession();
    }

}