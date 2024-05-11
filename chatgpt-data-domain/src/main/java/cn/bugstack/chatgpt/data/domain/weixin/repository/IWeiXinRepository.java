package cn.bugstack.chatgpt.data.domain.weixin.repository;

import java.util.List;

/**
 * @description 微信服务仓储
 */
public interface IWeiXinRepository {

    String genCode(String openId);

    List<String> historyMsg(String openId, String msg);

}
