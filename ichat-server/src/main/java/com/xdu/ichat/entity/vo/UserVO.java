package com.xdu.ichat.entity.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author hujiaqi
 * @create 2020/6/27
 */

@Getter
@Setter
@ToString
public class UserVO {

    private String nick;

    private String username;

    private String password;

    private String captchaId;

    private String captcha;
}
