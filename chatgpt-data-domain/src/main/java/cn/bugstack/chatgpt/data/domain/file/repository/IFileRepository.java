package cn.bugstack.chatgpt.data.domain.file.repository;

import org.springframework.web.multipart.MultipartFile;

public interface IFileRepository {
    Boolean save(MultipartFile multipartFile, String hash);

    String getCSV(String hash);
}
