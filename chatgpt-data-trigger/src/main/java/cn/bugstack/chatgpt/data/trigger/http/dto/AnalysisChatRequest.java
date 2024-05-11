package cn.bugstack.chatgpt.data.trigger.http.dto;

import cn.bugstack.chatgpt.data.types.enums.ChatGPTModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisChatRequest {
    private String model = ChatGPTModel.GPT_3_5_TURBO.getCode();
    private String name;
    /**
     * 分析目标
     */
    private MessageEntity goal;

    /**
     * 图表类型
     */
    private String chartType;
    /**
     * 文件hash
     */
    private String filehash;
}
