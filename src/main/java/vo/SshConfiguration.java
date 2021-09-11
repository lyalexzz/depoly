package vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @program: deploy
 * @description:
 * @author: LiYu
 * @create: 2021-09-10 20:40
 **/
@Data
@Accessors(chain = true)
public class SshConfiguration implements Serializable {
    /**
     * IP地址
     */
    private String ipAddress;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String pwd;
    /**
     * 脚本路径
     */
    private String path;
    /**
     * 端口号
     */
    private Integer port;
}
