package com.mall.service.impl;

import com.google.common.collect.Lists;
import com.mall.service.IFileService;
import com.mall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传实现类
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     *  为了防止同名文件的覆盖，我们给每个上传的文件重新命名
     * @param file MultipartFile
     * @param path upload文件夹地址
     * @return 返回上传后的文件名
     */
    @Override
    public String upload(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();
        // 扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        // 保证名字的唯一性
        String uploadName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件，文件的文件名:{},上传的路径:{},新文件名:{}", fileName,path,uploadName);

        File fileDir = new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path, uploadName);

        try {
            file.transferTo(targetFile); //到这一步文件上传成功（即到upload文件夹下)
            FTPUtil.uploadFile(Lists.newArrayList(targetFile)); //到这文件已经上传到ftp服务器
            targetFile.delete(); // 上传完之后，删除upload下面的文件
        } catch (IOException e) {
            logger.error("上传文件异常", e);
            return null;
        }
        return targetFile.getName();
    }
}
