package com.xdu.ichat.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */

@Getter
@Setter
@ToString
public class User {

    private Long uid;

    private String avatar;

    private String nick;

    private String username;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private Date createTime;

    @JsonIgnore
    private Date updateTime;
}
