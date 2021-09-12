package util;

import com.jcraft.jsch.*;
import vo.SshConfiguration;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * @program: TranscodingAnalyzer
 * @description: 链接操作类
 * @author: LiYu
 * @create: 2021-09-10 18:20
 **/
public class TranscodingAnalyzer {

    private ChannelSftp channelSftp;
    /**
     * 链接对话
     */
    private Session session = null;

    public TranscodingAnalyzer(SshConfiguration conf) throws JSchException {
        //创建JSch对象
        JSch jSch = new JSch();
        //根据用户名，主机ip和端口获取一个Session对象
        session = jSch.getSession(conf.getUserName(), conf.getIpAddress(), conf.getPort());
        //设置密码
        session.setPassword(conf.getPwd());
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        //为Session对象设置properties
        session.setConfig(config);
        //设置超时
        int timeout = 60000;
        session.setTimeout(timeout);
        //通过Session建立连接
        session.connect();

    }

    /**
     * 遍历文件名
     *
     * @param directory 文件地址
     * @return 文件列表
     * @throws Exception 文件地址不存在
     */
    public List<String> listFiles(String directory) throws Exception {
        //通道
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        //连接通道
        channelSftp.connect();
        Vector fileList;
        List<String> fileNameList = new ArrayList<String>();
        fileList = channelSftp.ls(directory);
        for (Object o : fileList) {
            String fileName = ((ChannelSftp.LsEntry) o).getFilename();
            if (".".equals(fileName) || "..".equals(fileName)) {
                continue;
            }
            if (fileName.charAt(0) != '.') {
                fileNameList.add(fileName);
            }
            channelSftp.quit();
            this.close();
        }
        return fileNameList;
    }

    public void close() {
        session.disconnect();
    }

    /**
     * 查看文件
     *
     * @param src 路径
     * @param filename 文件名
     * @return 文件内容
     * @throws JSchException 异常
     * @throws SftpException 异常
     * @throws InterruptedException 异常
     * @throws IOException 异常
     */

    public String catFile(String src, String filename) throws JSchException, SftpException, InterruptedException, IOException {

        //通道
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        //连接通道
        channelSftp.connect();
        StringBuilder sb = new StringBuilder();
        Channel channel = session.openChannel("exec");
        ChannelExec channelExec = (ChannelExec) channel;
        String cmdGet = "cat " + filename;
        channelExec.setCommand("cd " + src + ";" + cmdGet);
        channelExec.setInputStream(null);
        BufferedReader input = new BufferedReader(new InputStreamReader
                (channelExec.getInputStream()));
        channelExec.connect();
        String line = "";
        while ((line = input.readLine()) != null) {
            sb.append(line).append("\n");
        }
        // 关闭通道
        channelExec.disconnect();
        input.close();
        //关闭session
        session.disconnect();
        channelSftp.quit();
        return sb.toString();
    }

    /**
     * 执行shell命令
     *
     * @param command 命令
     * @param news 消息列表
     */
    public void execute(final String command, JTextArea news) {
        int returnCode = 0;
        try {
            //打开通道，设置通道类型，和执行的命令
            Channel channel = session.openChannel("exec");
            ChannelExec channelExec = (ChannelExec) channel;
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            BufferedReader input = new BufferedReader(new InputStreamReader
                    (channelExec.getInputStream()));
            channelExec.connect();
            //接收远程服务器执行命令的结果
            String line;
            int i = 0;
            while ((line = input.readLine()) != null) {
                if(i == 0){
                    news.setText(command+"\n"+line);
                }else{
                    news.setText(news.getText()+"\n"+line);
                }
                i++;
            }
            input.close();
            // 得到returnCode
            if (channelExec.isClosed()) {
                returnCode = channelExec.getExitStatus();
            }
            // 关闭通道
            channelExec.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载单个文件，如果指定文件名，则下载到文件名否则保持原有文件名
     *
     * @param remoteFilePath    远程文件路径 /tmp/xxx.txt || xxx.txt.zip
     * @param localFilePath     本地文件路径 如 D:\\xxx.txt
     * @return 下载的文件
     */
    public File downloadFile(String remoteFilePath, String localFilePath) {
        String remoteFileName = "";
        // 远端目录确定以 / 作为目录格式
        String rFileSeparator = "/";
        int rDirNameSepIndex = remoteFilePath.lastIndexOf(rFileSeparator) + 1;
        String rDir = remoteFilePath.substring(0, rDirNameSepIndex);
        remoteFileName = remoteFilePath.substring(rDirNameSepIndex);
        if(localFilePath.endsWith(File.separator)) {
            localFilePath = localFilePath + (localFilePath.endsWith(File.separator) ? remoteFileName : "/" + remoteFileName);
        }
        File file = null;
        OutputStream output = null;
        try {
            file = new File(localFilePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            channelSftp.cd(rDir);
            output = new FileOutputStream(file);
            channelSftp.get(remoteFileName, output);
        } catch (SftpException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            close();
        }

        return file;
    }

    /**
     * 上传单个文件，如果指正下载文件名则使用，否则保留原有文件名
     *
     * @param remoteFilePath    远程文件路径 /tmp/xxx.txt ||xxx.txt.zip
     * @param uploadFilePath    要上传的文件 如：D:\\test\\xxx.txt
     */
    public void uploadFile(String remoteFilePath, String uploadFilePath) {
        FileInputStream in = null;
        String remoteFileName = "";
        String remoteDir = remoteFilePath;
        String localFileName = "";
        // 远端目录确定以 / 作为目录格式
        String rFileSeparator = "/";
        if(remoteFilePath.endsWith(rFileSeparator)) {
            localFileName = uploadFilePath.substring(uploadFilePath.lastIndexOf(File.separator) + 1);
            remoteFileName = localFileName;
        } else {
            int fileNameDirSep = remoteFilePath.lastIndexOf(rFileSeparator) + 1;
            remoteDir = remoteFilePath.substring(0, fileNameDirSep);
            remoteFileName = remoteFilePath.substring(fileNameDirSep);
        }
        try {
            channelSftp.cd(remoteDir);
        } catch (SftpException e) {
            try {
                channelSftp.mkdir(remoteDir);
                channelSftp.cd(remoteDir);
            } catch (SftpException e1) {
                throw new RuntimeException("ftp创建文件路径失败" + remoteDir);
            }
        }
        File file = new File(uploadFilePath);
        try {
            in = new FileInputStream(file);
            channelSftp.put(in, remoteFileName);
        } catch (FileNotFoundException e) {
        } catch (SftpException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            close();
        }
    }

}