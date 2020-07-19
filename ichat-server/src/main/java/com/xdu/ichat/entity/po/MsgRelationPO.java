package com.xdu.ichat.entity.po;

import lombok.*;

import java.util.Date;

/**
 * @author hujiaqi
 * @create 2020/6/29
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MsgRelationPO {

    private Long ownerUid;

    private Long otherUid;

    private Long mid;

    private Integer type;

    private Date createTime;

    public MsgRelationPO(Long ownerUid, Long otherUid, Long mid, Integer type) {
        this.ownerUid = ownerUid;
        this.otherUid = otherUid;
        this.mid = mid;
        this.type = type;
    }
}
