package cn.bugstack.chatgpt.data.domain.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 鉴权结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthStateEntity {

    private String code;
    private String info;
    private String openId;
    private String token;

}
