package com.xdu.ichat.entity.vo;

import com.xdu.ichat.entity.User;
import com.xdu.ichat.entity.po.MsgContactPO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author hujiaqi
 * @create 2020/6/29
 */

@Getter
@Setter
@AllArgsConstructor
public class MsgContactVO {
    private User owner;

    private Long totalUnread;

    private List<MsgContactPO> msgList;
}
