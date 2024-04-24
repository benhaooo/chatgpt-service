package cn.bugstack.chatgpt.data.domain.openai.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptEntity {
    private Integer id;
    private String name;
    private String description;
    private String content;
    private String tag;
}
