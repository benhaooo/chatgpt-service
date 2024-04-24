package cn.bugstack.chatgpt.data.domain.file.service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    Boolean save(MultipartFile multipartFile,String hash);

    String getFileUrl(String hash);
}
