import com.jcraft.jsch.*;
import vo.SshConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class TranscodingAnalyzer {

    private ChannelSftp channelSftp;
    /**
     * 链接对话
     */
    private Session session = null;
    /**
     * 超时时间
     */
    private int timeout = 60000;

    public ArrayList<String> stdout = new ArrayList<>();


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
        Iterator it = fileList.iterator();
        while (it.hasNext()) {
            String fileName = ((ChannelSftp.LsEntry) it.next()).getFilename();
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
     * @param src
     * @param filename
     * @return
     * @throws JSchException
     * @throws SftpException
     * @throws InterruptedException
     * @throws IOException
     */

    public String catFile(String src, String filename) throws JSchException, SftpException, InterruptedException, IOException {

        //通道
        channelSftp = (ChannelSftp) session.openChannel("sftp");
        //连接通道
        channelSftp.connect();
        StringBuffer sb = new StringBuffer();
        try {
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
                sb.append(line + "\n");
            }
            System.out.println(sb);
            // 关闭通道
            channelExec.disconnect();
            input.close();
            //关闭session
            session.disconnect();
            channelSftp.quit();
        } catch (Exception e) {
            throw e;
        }
        return sb.toString();

    }

    /**
     * 执行shell命令
     *
     * @param command
     * @return
     */
    public int execute(final String command) {
        int returnCode = 0;
        JSch jsch = new JSch();
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
            while ((line = input.readLine()) != null) {
                stdout.add(line);
            }
            input.close();
            // 得到returnCode
            if (channelExec.isClosed()) {
                returnCode = channelExec.getExitStatus();
            }
            // 关闭通道
            channelExec.disconnect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnCode;
    }
}