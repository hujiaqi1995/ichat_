package com.xdu.ichat.controller;

import com.xdu.ichat.common.CommonResult;
import com.xdu.ichat.entity.vo.ContactVO;
import com.xdu.ichat.entity.vo.MsgContactVO;
import com.xdu.ichat.service.ContactService;
import com.xdu.ichat.service.MsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hujiaqi
 * @create 2020/6/29
 */

@RestController
@RequestMapping("/msg")
@CrossOrigin
public class MsgController {

    @Autowired
    private MsgService msgService;

    @Autowired
    private ContactService contactService;

    @RequestMapping(value = "/msgAndContact", method = RequestMethod.GET)
    public CommonResult<?> queryMsgAndContacts(HttpServletRequest request) {
        Long ownerUid = (Long) request.getSession().getAttribute("uid");
        MsgContactVO msgContactVO = msgService.queryMsgContacts(ownerUid);
        ContactVO contactVO = contactService.queryContacts(ownerUid);
        Map<String, Object> map = new HashMap<>();
        map.put("msgContact", msgContactVO);
        map.put("contact", contactVO);
        return CommonResult.success(map, "查询消息联系人成功!");
    }
}
