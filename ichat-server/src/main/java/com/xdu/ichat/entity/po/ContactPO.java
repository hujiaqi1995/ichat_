package com.xdu.ichat.entity.po;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author hujiaqi
 * @create 2020/6/29
 */

@Getter
@Setter
public class ContactPO {
    private Long ownerUid;

    private Long otherUid;

    private Date createTime;
}
