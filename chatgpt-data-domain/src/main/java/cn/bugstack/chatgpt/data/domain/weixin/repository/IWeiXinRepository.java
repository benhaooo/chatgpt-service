package cn.bugstack.chatgpt.data.domain.weixin.repository;

/**
 * @description 微信服务仓储
 */
public interface IWeiXinRepository {

    String genCode(String openId);

}
