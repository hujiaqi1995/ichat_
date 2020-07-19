package com.xdu.ichat.entity.po;

import lombok.*;

import java.util.Date;

/**
 * @author hujiaqi
 * @create 2020/6/28
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MsgPO {
    private Long mid;

    private String content;

    private Date createTime;

    private Integer type;
}
