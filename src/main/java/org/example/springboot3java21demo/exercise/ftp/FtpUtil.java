package org.example.springboot3java21demo.exercise.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;


/**
 * FTP工具类
 */
public class FtpUtil {
    private static final Logger log = LoggerFactory.getLogger(FtpUtil.class);
    public FTPClient ftpClient = new FTPClient();

    /**
     * 建立FTP链接，FTP服务器地址、端口、登陆用户信息都在配置里配置即可。
     *
     * @throws IOException
     */
    public boolean connectFtp(String ftpAddress, String ftpPort, String frpUserName, String frpPassword) throws IOException {
        log.info("*****连接FTP服务器...*****");
        try {
            ftpClient.connect(ftpAddress, Integer.valueOf(ftpPort).intValue());
            ftpClient.setControlEncoding("GB2312");
            int reply = ftpClient.getReplyCode();
            if (FTPReply.isPositiveCompletion(reply)) {
                if (ftpClient.login(frpUserName, frpPassword)) {
                    log.info("*****连接FTP服务器成功！*****");
                    return true;
                }
            } else {
                log.error("*****连接失败!响应代码为【" + reply + "】*****");
            }
            disconnect();
        } catch (Exception e) {
            log.error("*****连接失败：" + e.getMessage());
        }
        return false;
    }

    /**
     * 设置FTP客户端 被动模式、数据模式为二进制、字符编码GBK
     */
    public void setConnectType() {
        try {
            ftpClient.enterLocalPassiveMode();
            ftpClient.setDefaultTimeout(1000 * 120);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setControlEncoding("GB2312");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 断开与远程服务器的连接
     *
     * @throws IOException
     */
    public void disconnect() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void ftpConnectionTest() throws IOException {
        connectFtp("172.16.11.7", "21", "ftp", "ftp");
        boolean downloads = downloads(".*hahV_FILE_IDA202012081221.*.019.gz", 22, 25, "/home/ftp/ltl");
        System.out.println(downloads);
    }

    public boolean downloads(String pattern, int start, int end, String remotePath) throws IOException {
        int count = 0;
        int subCount = 0;
//        setConnectType();
        ftpClient.changeWorkingDirectory(remotePath);
        FTPFile[] files = ftpClient.listFiles();
        if (files.length == 0) {
            log.info("*****未在服务器上找到文件！*****");
            return false;
        } else {//目录下有文件
            for (int i = 0; i < files.length; i++) {
                FTPFile ftpFile = files[i];
                String fileName = ftpFile.getName();
                boolean isMatch = Pattern.matches(pattern, fileName);
                if (isMatch) {
                    count++;
                    subCount = Integer.parseInt(fileName.substring(start, end));
                }
            }
        }
        System.out.println(count);
        System.out.println(subCount);
        return 0 != count && count == subCount;
    }

    /**
     * 上传标志文件
     *
     * @param localFileName
     * @param remoteFileName
     * @return
     */
    public boolean upload(String localFileName, String remoteFileName) {

        boolean b = false;
        try {
            File file = new File(localFileName);
            FileInputStream input = new FileInputStream(file);
            b = ftpClient.changeWorkingDirectory(remoteFileName);
            log.info("*****改变目录是否成功：" + b);
            String remoteFile = remoteFileName + file.getName();
            b = ftpClient.storeFile(new String(remoteFile.getBytes("GB2312"), StandardCharsets.ISO_8859_1), input);
            if (b) {
                log.info(" ****** 标志文件" + localFileName + "上传成功!");
            }
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }
}
