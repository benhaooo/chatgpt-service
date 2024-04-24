package cn.bugstack.chatgpt.data.infrastructure.repository;

import cn.bugstack.chatgpt.data.domain.openai.model.entity.PromptEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.entity.UserAccountQuotaEntity;
import cn.bugstack.chatgpt.data.domain.openai.model.valobj.UserAccountStatusVO;
import cn.bugstack.chatgpt.data.domain.openai.repository.IOpenAiRepository;
import cn.bugstack.chatgpt.data.infrastructure.dao.IOpenAIPromptDao;
import cn.bugstack.chatgpt.data.infrastructure.dao.IUserAccountDao;
import cn.bugstack.chatgpt.data.infrastructure.po.OpenAIPromptPO;
import cn.bugstack.chatgpt.data.infrastructure.po.UserAccountPO;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @description OpenAi 仓储服务
 */
@Repository
public class OpenAiRepository implements IOpenAiRepository {

    @Resource
    private IUserAccountDao userAccountDao;
    @Resource
    private IOpenAIPromptDao openAIPromptDao;


    @Override
    public int subAccountQuota(String openai) {
        return userAccountDao.subAccountQuota(openai);
    }

    @Override
    public UserAccountQuotaEntity queryUserAccount(String openid) {
        UserAccountPO userAccountPO = userAccountDao.queryUserAccount(openid);
        if (null == userAccountPO) return null;
        UserAccountQuotaEntity userAccountQuotaEntity = new UserAccountQuotaEntity();
        userAccountQuotaEntity.setOpenid(userAccountPO.getOpenid());
        userAccountQuotaEntity.setTotalQuota(userAccountPO.getTotalQuota());
        userAccountQuotaEntity.setSurplusQuota(userAccountPO.getSurplusQuota());
        userAccountQuotaEntity.setUserAccountStatusVO(UserAccountStatusVO.get(userAccountPO.getStatus()));
        userAccountQuotaEntity.genModelTypes(userAccountPO.getModelTypes());
        return userAccountQuotaEntity;
    }
    @Override
    public List<PromptEntity> queryPromptList() {
        List<OpenAIPromptPO> openAIPromptPOList = openAIPromptDao.queryPromptList();
        List<PromptEntity> promptEntityList = new ArrayList<>(openAIPromptPOList.size());
        for (OpenAIPromptPO openAIPromptPO : openAIPromptPOList) {
            PromptEntity promptEntity = new PromptEntity();
            promptEntity.setId(openAIPromptPO.getId());
            promptEntity.setName(openAIPromptPO.getName());
            promptEntity.setDescription(openAIPromptPO.getDescription());
            promptEntity.setContent(openAIPromptPO.getContent());
            promptEntity.setTag(openAIPromptPO.getTag());
            promptEntityList.add(promptEntity);
        }
        return promptEntityList;
    }


}
