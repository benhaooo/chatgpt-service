package cn.bugstack.chatgpt.data.domain.file.service;

import cn.bugstack.chatgpt.data.domain.file.repository.IFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService implements IFileService {

    @Autowired
    private IFileRepository fileRepository;

    @Override
    public Boolean save(MultipartFile multipartFile, String hash) {
        Boolean save = fileRepository.save(multipartFile, hash);
        return save;
    }

    @Override
    public String getFileUrl(String hash) {
        return fileRepository.getCSV(hash);
    }

}
