package cn.bugstack.chatgpt.data.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromptDTO {
    private Integer id;
    private String name;
    private String description;
    private String content;
    private String tag;
}
