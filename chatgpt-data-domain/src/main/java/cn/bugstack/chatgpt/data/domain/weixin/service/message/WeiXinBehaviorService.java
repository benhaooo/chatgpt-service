package cn.bugstack.chatgpt.data.domain.weixin.service.message;

import cn.bugstack.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.MessageEntity;
import cn.bugstack.chatgpt.data.domain.openai.service.IChatService;
import cn.bugstack.chatgpt.data.domain.weixin.model.entity.MessageTextEntity;
import cn.bugstack.chatgpt.data.domain.weixin.model.entity.UserBehaviorMessageEntity;
import cn.bugstack.chatgpt.data.domain.weixin.model.valobj.MsgTypeVO;
import cn.bugstack.chatgpt.data.domain.weixin.repository.IWeiXinRepository;
import cn.bugstack.chatgpt.data.domain.weixin.service.IWeiXinBehaviorService;
import cn.bugstack.chatgpt.data.types.exception.ChatGPTException;
import cn.bugstack.chatgpt.data.types.sdk.weixin.XmlUtil;
import com.google.common.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @description 受理用户行为接口实现类
 */
@Service
public class WeiXinBehaviorService implements IWeiXinBehaviorService {

    @Value("${wx.config.originalid}")
    private String originalId;

    @Resource
    private Cache<String, String> codeCache;
    @Resource
    private IWeiXinRepository repository;
    @Autowired
    private IChatService chatService;

    /**
     * 1. 用户的请求行文，分为事件event、消息text，这里我们只处理消息内容
     * 2. 用户行为、消息类型，是多样性的，这部分如果用户有更多的扩展需求，可以使用设计模式【模板模式 + 策略模式 + 工厂模式】，分拆逻辑。
     */

//    private IRedisService redisService;
    @Override
    public String acceptUserBehavior(UserBehaviorMessageEntity userBehaviorMessageEntity) {
        // Event 事件类型，忽略处理
        if (MsgTypeVO.EVENT.getCode().equals(userBehaviorMessageEntity.getMsgType())) {
            return "";
        }

        // Text 文本类型
        if (MsgTypeVO.TEXT.getCode().equals(userBehaviorMessageEntity.getMsgType())) {

            if ("1".equals(userBehaviorMessageEntity.getContent())) {
                // 生成验证码
                String code = repository.genCode(userBehaviorMessageEntity.getOpenId());

                // 反馈信息[文本]
                MessageTextEntity res = new MessageTextEntity();
                res.setToUserName(userBehaviorMessageEntity.getOpenId());
                res.setFromUserName(originalId);
                res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
                res.setMsgType("text");
                res.setContent(String.format("您的验证码为：%s 有效期%d分钟！", code, 3));
                return XmlUtil.beanToXml(res);
            } else {
                List<String> msgList = repository.historyMsg(userBehaviorMessageEntity.getOpenId(), userBehaviorMessageEntity.getContent());
                ArrayList<MessageEntity> messages = new ArrayList<>();
                for (String msg : msgList) {
                    messages.add(MessageEntity.builder()
                            .content(msg)
                            .role("user")
                            .name(userBehaviorMessageEntity.getOpenId())
                            .build());
                }
                messages.add(MessageEntity.builder()
                        .content(userBehaviorMessageEntity.getContent())
                        .role("user")
                        .name(userBehaviorMessageEntity.getOpenId())
                        .build());
                ChatProcessAggregate chatProcessAggregate = ChatProcessAggregate.builder()
                        .messages(messages)
                        .model("gpt-3.5-turbo")
                        .build();
//                ChatCompletionResponse completions = chatService.completions(chatProcessAggregate);
//                String content = completions.getChoices().get(0).getMessage().getContent();
                MessageTextEntity res = new MessageTextEntity();
                res.setToUserName(userBehaviorMessageEntity.getOpenId());
                res.setFromUserName(originalId);
                res.setCreateTime(String.valueOf(System.currentTimeMillis() / 1000L));
                res.setMsgType("text");
//                res.setContent(content);
                return XmlUtil.beanToXml(res);
            }

        }

        throw new ChatGPTException(userBehaviorMessageEntity.getMsgType() + " 未被处理的行为类型 Err！");
    }

}
