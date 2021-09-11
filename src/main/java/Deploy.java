import vo.SshConfiguration;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * @program: deploy
 * @description: 程序入口
 * @author: LiYu
 * @create: 2021-09-10 18:24
 **/
public class Deploy {
    TranscodingAnalyzer transcodingAnalyzer = null;
    //初始化配置对象
    SshConfiguration sshConfiguration = null;

    public static void main(String[] args) {
        Deploy lo = new Deploy();
        lo.showUI();
    }

    public void showUI() {
        //窗体类
        javax.swing.JFrame jf = new javax.swing.JFrame();
        //窗体名称
        jf.setTitle("脚本执行器");
        //窗体大小（具体值跟电脑显示器的像素有关，可调整到合适大小）
        jf.setSize(400, 600);
        //设置退出进程的方法
        jf.setDefaultCloseOperation(3);
        //设置居中显示用3
        jf.setLocationRelativeTo(null);

        //流式布局管理器
        java.awt.FlowLayout flow = new java.awt.FlowLayout();
        //给窗体设置为流式布局——从左到右然后从上到下排列自己写的组件顺序
        jf.setLayout(flow);
        jf.add(new JLabel("    服务器IP:"));
        //文本框
        javax.swing.JTextField ipAddress = new javax.swing.JTextField("www.ljfchtcc.com");
        java.awt.Dimension dm = new java.awt.Dimension(180, 30);
        ipAddress.setPreferredSize(dm);
        jf.add(ipAddress);
        jf.add(new JLabel(":"));
        javax.swing.JTextField port = new JTextField("22");
        java.awt.Dimension dmPort = new java.awt.Dimension(58, 30);
        port.setPreferredSize(dmPort);
        jf.add(port);
        jf.add(new JLabel("服务器账户:"));
        javax.swing.JTextField userName = new javax.swing.JTextField("root");
        java.awt.Dimension dmUserName = new java.awt.Dimension(250, 30);
        userName.setPreferredSize(dmUserName);
        jf.add(userName);
        jf.add(new JLabel("服务器密码:"));
        javax.swing.JTextField pwd = new javax.swing.JTextField("");
        java.awt.Dimension dmPwd = new java.awt.Dimension(250, 30);
        pwd.setPreferredSize(dmPwd);
        jf.add(pwd);
        jf.add(new JLabel("    脚本路径:"));
        javax.swing.JTextField path = new javax.swing.JTextField("/");
        java.awt.Dimension dmPath = new java.awt.Dimension(250, 30);
        path.setPreferredSize(dmPath);
        Document dt = path.getDocument();
        JComboBox<Object> fileList = new JComboBox<>();
        dt.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                getFileList(path, jf, fileList);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                getFileList(path, jf, fileList);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                getFileList(path, jf, fileList);
            }
        });
        jf.add(path);
        jf.add(new JLabel("    文件列表:"));
        java.awt.Dimension dmFileList = new java.awt.Dimension(250, 30);
        fileList.setPreferredSize(dmFileList);
        jf.add(fileList);
        jf.add(new JLabel("          消息框:"));
        JTextArea news = new JTextArea(5000000, 50000000);
        java.awt.Dimension dmNews = new java.awt.Dimension(250, 300);
        news.setPreferredSize(dmNews);
        news.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(news);
        Dimension size = news.getPreferredSize();
        jsp.setBounds(110, 90, size.width, size.height);
        jsp.setPreferredSize(dmNews);
        jf.add(jsp);
        JButton startConnecting = new JButton("加载配置");
        startConnecting.addActionListener(event ->
                startConnecting(jf, ipAddress, userName, pwd, path, port, fileList)
        );
        jf.add(startConnecting);
        JButton checkTheFile = new JButton("查看文件");
        checkTheFile.addActionListener(event ->
                checkTheFile(news, jf, Objects.requireNonNull(fileList.getSelectedItem()).toString(), path.getText())
        );
        jf.add(checkTheFile);
        JButton startExecution = new JButton("开始执行");
        startExecution.addActionListener(event ->
                startExecution(news, jf, Objects.requireNonNull(fileList.getSelectedItem()).toString(), path.getText())
        );
        jf.add(startExecution);
        //设置可见，放在代码最后一句
        jf.setVisible(true);
    }

    /**
     * 开始链接服务器
     *
     * @param jf        主体
     * @param ipAddress 服务器地址
     * @param userName  用户名
     * @param pwd       密码
     * @param path      文件路径
     * @param port      端口号
     * @param fileList  文件下拉框
     */
    public void startConnecting(JFrame jf, JTextField ipAddress, JTextField userName, JTextField pwd, JTextField path, JTextField port, JComboBox<Object> fileList) {
        if ("".equals(ipAddress.getText())) {
            JOptionPane.showMessageDialog(jf, "请输入IP地址", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("".equals(userName.getText())) {
            JOptionPane.showMessageDialog(jf, "请输入用户名", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("".equals(pwd.getText())) {
            JOptionPane.showMessageDialog(jf, "请输入密码", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("".equals(path.getText())) {
            JOptionPane.showMessageDialog(jf, "请输入脚本地址", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if ("".equals(port.getText())) {
            JOptionPane.showMessageDialog(jf, "请输入端口号", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        sshConfiguration = new SshConfiguration()
                .setUserName(userName.getText())
                .setPwd(pwd.getText())
                .setPort(Integer.parseInt(port.getText()))
                .setIpAddress(ipAddress.getText())
                .setPath(path.getText());
        getFileList(path, jf, fileList);
    }

    /**
     * 查看文件
     *
     * @param news     消息框
     * @param fileName 文件名
     * @param path     文件路径
     * @param jf       主体
     */
    public void checkTheFile(JTextArea news, JFrame jf, String fileName, String path) {
        try {
            createLink(jf);
            String content = transcodingAnalyzer.catFile(path, fileName);
            news.setText("");
            news.setText(content);
        } catch (Exception e) {
            news.setText(e.toString());
            JOptionPane.showMessageDialog(jf, "文件查看出错！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 开始执行
     *
     * @param news     消息框
     * @param jf       主体
     * @param fileName 文件名
     * @param path     路径
     */
    public void startExecution(JTextArea news, JFrame jf, String fileName, String path) {
        createLink(jf);
        transcodingAnalyzer.execute("sh " + path + "/" + fileName);
        news.setText(transcodingAnalyzer.stdout.toString());
    }

    public void getFileList(JTextField path, JFrame jf, JComboBox<Object> fileList) {
        try {
            createLink(jf);
            //获取目录下面的所有文件
            List<String> fileNames = transcodingAnalyzer.listFiles(path.getText());
            fileList.setModel(new DefaultComboBoxModel<>(fileNames.toArray()));
        } catch (Exception e) {
            fileList.setModel(new DefaultComboBoxModel<>());
        }
    }

    private void createLink(JFrame jf) {
        try {
            if (sshConfiguration != null) {
                //开始链接服务器
                transcodingAnalyzer = new TranscodingAnalyzer(sshConfiguration);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(jf, "链接服务器失败，请检查配置是否正确！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
