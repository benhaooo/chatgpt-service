package cn.bugstack.chatgpt.data.infrastructure.repository;

import cn.bugstack.chatgpt.data.domain.file.repository.IFileRepository;
import cn.bugstack.chatgpt.data.infrastructure.redis.IRedisService;
import cn.bugstack.chatgpt.data.types.sdk.common.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;


@Repository
public class FileRepository implements IFileRepository {

    @Autowired
    private IRedisService redisService;
    private static final String R_CSV = "R_CSV";

    @Override
    public Boolean save(MultipartFile multipartFile, String hash) {
        String csv = ExcelUtils.excelToCsv(multipartFile);
        redisService.addToMap(R_CSV, hash, csv);
        return true;
    }

    @Override
    public String getCSV(String hash) {
        return redisService.getFromMap(R_CSV,hash);
    }


}
