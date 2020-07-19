package com.xdu.ichat.entity.vo;

import com.xdu.ichat.entity.User;
import com.xdu.ichat.entity.po.MsgPO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MsgVO {
    private User owner;

    private User other;

    private List<MsgPO> messageList;
}
