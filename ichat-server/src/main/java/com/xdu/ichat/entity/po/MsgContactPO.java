package com.xdu.ichat.entity.po;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author hujiaqi
 * @create 2020/6/29
 */

@Getter
@Setter
@ToString
public class MsgContactPO {
    private Long uid;

    private String nick;

    private String username;

    private String avatar;

    private Date createTime;

    private String content;

    private Long unread;
}
