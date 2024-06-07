package com.qiu.qoj.service;

import com.qiu.qoj.model.dto.file.UploadFileRequest;
import com.qiu.qoj.model.enums.FileUploadBizEnum;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FileService {
    void downloadFile(String filepath, HttpServletResponse response);

    String uploadFile(MultipartFile multipartFile, UploadFileRequest uploadFileRequest, HttpServletRequest request);

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum);

}
