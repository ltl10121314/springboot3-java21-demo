package org.example.springboot3java21demo.exercise.ftp;


import com.jcraft.jsch.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * SFTP 工具类
 */
public class SFfpUtil {
    private static Session sshSession;
    private static final Logger log = LoggerFactory.getLogger(SFfpUtil.class);

    /**
     * 连接sftp服务器
     *
     * @param host     ftp地址
     * @param port     端口
     * @param userName 账号
     * @param password 密码
     * @return
     */
    public static ChannelSftp sftpConnection(String host, int port, String userName, String password) throws Exception {
        JSch jsch = new JSch();
        ChannelSftp channelSftp;
        try {
            jsch.getSession(userName, host, port);
            sshSession = jsch.getSession(userName, host, port);
            sshSession.setPassword(password);
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(properties);
            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
            throw new Exception("Sftp服务器登录异常!");
        }
        return channelSftp;
    }

    /**
     * @return
     * @description 退出Sftp服务器登录
     **/
    public static void sftpClose(ChannelSftp channelSftp) {
        if (channelSftp != null) {
            if (channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * 关闭session
     */
    public static void sessionClose() {
        if (sshSession != null) {
            if (sshSession.isConnected()) {
                sshSession.disconnect();
                sshSession = null;
            }
        }
    }

    /**
     * 下载sftp文件
     *
     * @param sftp
     * @param newFileName 新文件名称
     * @param path        文件路径
     * @param fileName    文件名称
     * @param downUrl     下载到本地的路径
     * @throws Exception
     */
    public static void downSftpFile(ChannelSftp sftp, String newFileName, String path, String fileName, String downUrl) throws Exception {

        OutputStream os = null;
        try {
            File localFile = new File(downUrl + "/" + newFileName);
            if (!localFile.getParentFile().exists()) {
                localFile.getParentFile().mkdirs();
                localFile.createNewFile();
            }

            if (path != null && !"".equals(path)) {
                //进入所在路径
                sftp.cd(path);
            }
            os = new FileOutputStream(localFile);
            sftp.get(path + fileName, os);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean fileExit(String pattern, int start, int end, String remotePath) throws Exception {
        int count = 0;
        int subCount = 0;
        ChannelSftp channelSftp = sftpConnection("39.106.30.70", 22, "mysftp", "ltl10121314");
        try {
            channelSftp.cd(remotePath);
            Vector files = channelSftp.ls("*");
            List<String> ret = new ArrayList<String>();
            if (files.size() == 0) {
                //目录下没有文件
                log.info("*****未在服务器上找到文件！*****");
                return false;
            } else {
                for (int i = 0; i < files.size(); i++) {
                    Object obj = files.elementAt(i);
                    if (obj instanceof com.jcraft.jsch.ChannelSftp.LsEntry) {
                        ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
                        if (!entry.getAttrs().isDir()) {
                            String fileName = entry.getFilename();
                            boolean isMatch = Pattern.matches(pattern, fileName);
                            if (isMatch) {
                                count++;
                                subCount = Integer.parseInt(fileName.substring(start, end));
                            }
                        }
                    }
                }
            }
            log.info("--------------------{}下文件块总数：{}；目前到达的文件块数量：{}", remotePath, count, subCount);
            channelSftp.disconnect();
        } catch (Exception e) {
            log.error("", e);
            if (e.getMessage().contains("450")) {
                return false;
            } else if (e.getMessage().contains("550")) {
                return false;
            } else {
                throw e;
            }
        }
        return 0 != count && count == subCount;
    }

    @Test
    public void fileExitTest() throws Exception {
        boolean fileExit = fileExit(".*hahV_FILE_IDA202012081004.*.019.gz", 22, 25, "/upload");
        System.out.println(fileExit);
    }
}
