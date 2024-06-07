package com.qiu.qoj.controller;

import com.qiu.qoj.common.BaseResponse;
import com.qiu.qoj.common.ResultUtils;
import com.qiu.qoj.model.dto.file.UploadFileRequest;
import com.qiu.qoj.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 文件接口
 *
 * @author qiu
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {


    @Resource
    private FileService fileService;


    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String filePath = fileService.uploadFile(multipartFile, uploadFileRequest, request);
        return ResultUtils.success(filePath);
    }


}
