package cn.bugstack.chatgpt.data.trigger.http;

import cn.bugstack.chatgpt.data.domain.file.service.IFileService;
import cn.bugstack.chatgpt.data.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController()
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/file")

public class FileController {
    @Autowired
    private IFileService fileService;
    @PostMapping("/upload")
    public Response<String> uploadFile(@RequestPart("file") MultipartFile multipartFile, String hash, HttpServletRequest request) {
        fileService.save(multipartFile, hash);
        return null;
    }
}
