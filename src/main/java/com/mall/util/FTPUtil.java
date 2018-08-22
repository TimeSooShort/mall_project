package com.mall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * FTP工具类
 */
public class FTPUtil {
    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPwd = PropertiesUtil.getProperty("ftp.pass");

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    /**
     * 上传文件到ftp服务器
     * @param fileList 文件列表
     * @return 返回是否成功
     * @throws IOException
     */
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp, 21, ftpUser, ftpPwd);
        logger.info("开始链接ftp服务器");
        boolean result = ftpUtil.uploadFile("img", fileList);
        logger.info("结束上传");
        return result;
    }

    /**
     *  将文件上传到ftp服务器，而ftp服务器在linux上是个文件夹，你需要将文件上传到ftp文件夹下的某个文件夹里，
     *  remotePath就是这个文件夹名
     * @param remotePath 远程路径，上传到ftp文件下的{remotePath}文件里
     * @param fileList 文件列表
     * @return 返回是否成功
     * @throws IOException
     */
    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean upload = true;
        FileInputStream fis = null;
        if (connectServer(this.ip, this.port, this.user, this.pwd)){
            try {
                ftpClient.changeWorkingDirectory(remotePath); //更改工作目录，传空的话就不改变
                ftpClient.setBufferSize(1024); // 缓冲区
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); // 设置文件类型为二进制类型
                ftpClient.enterLocalPassiveMode(); // 被动模式
                for (File fileItem : fileList){
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(), fis);
                }
            } catch (IOException e) {
                logger.error("文件上传异常",e);
                upload = false;
                e.printStackTrace();
            } finally {
                fis.close();
                ftpClient.disconnect();
            }
        }
        return upload;
    }

    /**
     * ftp服务器的链接
     * @param ip ip
     * @param port 21
     * @param user 用户名
     * @param pwd 密码
     * @return
     */
    private boolean connectServer(String ip, int port, String user, String pwd){
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            logger.error("链接ftp服务器异常", e);
        }
        return isSuccess;
    }

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
