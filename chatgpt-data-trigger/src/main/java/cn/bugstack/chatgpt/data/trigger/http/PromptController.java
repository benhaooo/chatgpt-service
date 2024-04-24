package cn.bugstack.chatgpt.data.trigger.http;

import cn.bugstack.chatgpt.data.domain.openai.model.entity.PromptEntity;
import cn.bugstack.chatgpt.data.domain.openai.service.IChatService;
import cn.bugstack.chatgpt.data.trigger.http.dto.PromptDTO;
import cn.bugstack.chatgpt.data.types.common.Constants;
import cn.bugstack.chatgpt.data.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/prompt/")
public class PromptController {
    @Autowired
    IChatService chatService;

    @RequestMapping(value = "query_prompt_list", method = RequestMethod.GET)
    public Response<List<PromptDTO>> queryPromptList(){
        List<PromptEntity> promptEntityList = chatService.queryPromptList();
        List<PromptDTO> promptDTOList = new ArrayList<>(promptEntityList.size());
        for (PromptEntity openAIPromptPO : promptEntityList) {
            PromptDTO promptDTO = new PromptDTO();
            promptDTO.setId(openAIPromptPO.getId());
            promptDTO.setName(openAIPromptPO.getName());
            promptDTO.setDescription(openAIPromptPO.getDescription());
            promptDTO.setContent(openAIPromptPO.getContent());
            promptDTO.setTag(openAIPromptPO.getTag());
            promptDTOList.add(promptDTO);
        }
        return Response.<List<PromptDTO>>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info(Constants.ResponseCode.SUCCESS.getInfo())
                .data(promptDTOList)
                .build();
    }

}
