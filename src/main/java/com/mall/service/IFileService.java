package com.mall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Administrator on 2018/2/26.
 */
public interface IFileService {

    String upload(MultipartFile file, String path);
}
