import util.PageGenerationUtil;

import javax.swing.*;

/**
 * @program: deploy
 * @description: 程序入口
 * @author: LiYu
 * @create: 2021-09-10 18:24
 **/
public class Deploy {


    /**
     * 程序主入口
     * @param args
     */
    public static void main(String[] args) {
        Deploy lo = new Deploy();
        lo.loadPage();
    }

    /**
     * 主程序
     */
    public void loadPage() {
        //窗体类
        javax.swing.JFrame jf = new javax.swing.JFrame();
        //窗体名称
        jf.setTitle("脚本执行器");
        //窗体大小（具体值跟电脑显示器的像素有关，可调整到合适大小）
        jf.setSize(1000, 700);
        //设置退出进程的方法
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //设置居中显示用3
        jf.setLocationRelativeTo(null);
        PageGenerationUtil.initialization(jf);
        //设置可见，放在代码最后一句
        jf.setVisible(true);
    }

}
