package cn.bugstack.chatgpt.data.trigger.http;

import cn.bugstack.chatgpt.data.domain.auth.service.IAuthService;
import cn.bugstack.chatgpt.data.domain.file.service.IFileService;
import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.MessageEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.service.IChatService;
import cn.bugstack.chatgpt.data.trigger.http.dto.AnalysisChartDTO;
import cn.bugstack.chatgpt.data.trigger.http.dto.AnalysisChatRequest;
import cn.bugstack.chatgpt.data.trigger.http.dto.ChatGPTRequestDTO;
import cn.bugstack.chatgpt.data.trigger.http.dto.SaleProductDTO;
import cn.bugstack.chatgpt.data.types.common.Constants;
import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
import cn.bugstack.chatgpt.data.types.model.Response;
import cn.bugstack.chatgpt.data.types.sdk.common.ExcelUtils;
import cn.bugstack.chatgpt.domain.chat.ChatCompletionResponse;
import com.alibaba.fastjson.JSON;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description ChatGPT AI 服务
 */
@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/chatgpt/")
public class ChatGPTAIServiceController {

    @Resource
    private IChatService chatService;

    @Resource
    private IAuthService authService;

    @Autowired
    IFileService fileService;

    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5500")
    }, fallbackMethod = "completionsStreamError"
    )
    @RequestMapping(value = "chat/completions", method = RequestMethod.POST)
    public ResponseBodyEmitter completionsStream(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) {
        log.info("流式问答请求开始，使用模型：{} 请求信息：{}", request.getModel(), JSON.toJSONString(request.getMessages()));
        try {
            // 1. 基础配置；流式输出、编码、禁用缓存
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");

            // 2. 构建异步响应对象【对 Token 过期拦截】
            ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);
            boolean success = authService.checkToken(token);

            if (!success) {
                try {
                    emitter.send(Constants.ResponseCode.TOKEN_ERROR.getCode());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                emitter.complete();
                return emitter;
            }

            // 3. 获取 OpenID
            String openid = authService.openid(token);
            log.info("流式问答请求处理，openid:{} 请求模型:{}", openid, request.getModel());

            // 4. 构建参数
            ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                    .openid(openid)
                    .model(request.getModel())
                    .messages(request.getMessages().stream()
                            .map(entity -> MessageEntity.builder()
                                    .role(entity.getRole())
                                    .content(entity.getContent())
                                    .name(entity.getName())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();

            // 5. 请求结果&返回
            return chatService.completions(emitter, chatProcessAggregate);
        } catch (Exception e) {
            log.error("流式应答，请求模型：{} 发生异常", request.getModel(), e);
            throw new ChatGPTException(e.getMessage());
        }
    }

    final String prompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
            "分析需求：\n" +
            "{%s}\n" +
            "请使用：\n" +
            "{%s}\n" +
            "原始数据：\n" +
            "{%s}\n" +
            "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
            "【【【【【\n" +
            "{前端 Echarts V5 的 option 配置对象JSON代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
            "【【【【【\n" +
            "{明确的数据分析结论、越详细越好，不要生成多余的注释}";



    @RequestMapping(value = "analysis/chart", method = RequestMethod.POST)
    public Response<AnalysisChartDTO> analysisChart(@RequestBody AnalysisChatRequest request) {
        String csv = fileService.getFileUrl(request.getFilehash());
        log.info("分析图表请求开始，使用模型：{} 请求信息：{} csv：{}", request.getModel(), JSON.toJSONString(request.getGoal()), csv);

        ArrayList<MessageEntity> messages = new ArrayList<>();
        messages.add(MessageEntity.builder()
                .content(String.format(prompt, request.getGoal().getContent(), request.getChartType(), csv))
                .role(request.getGoal().getRole())
                .name(request.getGoal().getName())
                .build());
        ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                .model(request.getModel())
                .messages(messages)
                .build();
        ChatCompletionResponse completions = chatService.completions(chatProcessAggregate);
        String content = completions.getChoices().get(0).getMessage().getContent();
        AnalysisChartDTO analysisChartDTO = new AnalysisChartDTO();
        analysisChartDTO.setResult(content);
        return Response.<AnalysisChartDTO>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info(Constants.ResponseCode.SUCCESS.getInfo())
                .data(analysisChartDTO)
                .build();
    }



    @RequestMapping(value = "account", method = RequestMethod.GET)
    public Response<UserAccountQuotaEntity> queryUserAccount(@RequestHeader("Authorization") String token) {
        log.info("获取用户信息开始，token: {}", token);
        try {
            String openid = authService.openid(token);
            UserAccountQuotaEntity userAccountQuotaEntity = chatService.queryUserAccount(openid);
            log.info("获取用户信息完成，token: {} 结果: {}", token, userAccountQuotaEntity);
            return Response.<UserAccountQuotaEntity>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(userAccountQuotaEntity)
                    .build();
        }catch (Exception e){
            log.error("获取用户信息失败，token: {}", token);
            return Response.<UserAccountQuotaEntity>builder()
                    .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                    .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                    .build();
        }
    }

    public ResponseBodyEmitter completionsStreamError(@RequestBody ChatGPTRequestDTO request, @RequestHeader("Authorization") String token, HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        emitter.send("【错误编码】502" + "\r\n");
        emitter.send("【错误提示】网络超时，请重新对话尝试。");
        emitter.complete();
        return emitter;
    }

}
