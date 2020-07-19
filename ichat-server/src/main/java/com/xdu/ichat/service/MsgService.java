package com.xdu.ichat.service;

import com.google.gson.Gson;
import com.xdu.ichat.dao.MsgDao;
import com.xdu.ichat.dao.UserDao;
import com.xdu.ichat.entity.*;
import com.xdu.ichat.entity.po.MsgContactPO;
import com.xdu.ichat.entity.po.MsgContentPO;
import com.xdu.ichat.entity.po.MsgPO;
import com.xdu.ichat.entity.po.MsgRelationPO;
import com.xdu.ichat.entity.vo.MsgContactVO;
import com.xdu.ichat.entity.vo.MsgVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.xdu.ichat.utils.Constants.*;

/**
 * @author hujiaqi
 * @create 2020/6/28
 */

@Service
public class MsgService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private MsgDao msgDao;

    @Autowired
    private UserDao userDao;

    private Gson gson = new Gson();

    // 查找指定联系人所有消息
    public MsgVO queryConversationMsg(Long ownerUid, Long otherUid) {
        // 查询消息
        List<MsgPO> messageList = msgDao.findAll(ownerUid, otherUid);
        User owner = userDao.findByUid(ownerUid);
        User other = userDao.findByUid(otherUid);

        String currentUnreadString = redisService.getHash(ownerUid + CONVERSION_UNREAD_SUFFIX, otherUid + "");
        long currentUnread = StringUtils.isEmpty(currentUnreadString) ? 0 : Long.parseLong(currentUnreadString);

        // 将会话未读数设置为0
        redisService.deleteHashKey(ownerUid + CONVERSION_UNREAD_SUFFIX, otherUid + "");

        // 将总未读数减去会话未读数
        long totalUnread = queryTotalUnread(ownerUid);

        if (totalUnread - currentUnread <= 0) {
            redisService.deleteKey(ownerUid + TOTAL_UNREAD_SUFFIX);
        } else {
            redisService.incrementValue(ownerUid + TOTAL_UNREAD_SUFFIX, -currentUnread);
        }

        return new MsgVO(owner, other, messageList);
    }

    // 发送消息
    @Transactional
    public MsgVO sendNewMsg(long senderUid, long recipientUid, String content, int type) {
        // 存内容
        MsgContentPO msgContentPO = new MsgContentPO();
        msgContentPO.setContent(content);
        msgDao.insertMsgContent(msgContentPO);
        Long mid = msgContentPO.getMid();

        // 存发件人的发件箱，更新发件人的最近联系人
        MsgRelationPO msgRelationSender = new MsgRelationPO(senderUid, recipientUid, mid, MSG_SEND);
        msgDao.insertMsgRelation(msgRelationSender);

        MsgRelationPO msgRelation = msgDao.findMsgContact(senderUid, recipientUid);
        if (msgRelation == null) {
            msgDao.insertMsgContact(msgRelationSender);
        } else {
            msgDao.updateMsgContact(msgRelationSender);
        }

        // 存收件人的收件箱, 更新收件人的最近联系人
        MsgRelationPO msgRelationReceiver = new MsgRelationPO(recipientUid, senderUid, mid, MSG_RECEIVE);
        msgDao.insertMsgRelation(msgRelationReceiver);
        Date createTime = msgRelationReceiver.getCreateTime();

        msgRelation = msgDao.findMsgContact(recipientUid, senderUid);
        if (msgRelation == null) {
            msgDao.insertMsgContact(msgRelationReceiver);
        } else {
            msgDao.updateMsgContact(msgRelationReceiver);
        }

        // 更未读更新
        redisService.incrementValue(recipientUid + TOTAL_UNREAD_SUFFIX, 1);
        redisService.incrementHash(recipientUid + CONVERSION_UNREAD_SUFFIX, senderUid + "", 1);

        User sender = userDao.findByUid(senderUid);
        User receiver = userDao.findByUid(recipientUid);
        MsgPO msgPO = new MsgPO(mid, content, createTime, type);
        List<MsgPO> msgList = new ArrayList<>();
        msgList.add(msgPO);
        MsgVO msgVO = new MsgVO(sender, receiver, msgList);
        // 待推送消息发布到 redis
        redisService.convertAndSend(WEBSOCKET_MSG_TOPIC, gson.toJson(msgVO));
        return msgVO;
    }

    // 查找消息联系人
    public MsgContactVO queryMsgContacts(Long ownerId) {
        // 查找有最近一条消息的联系人
        User owner = userDao.findByUid(ownerId);
        List<MsgContactPO> contactMsgList = msgDao.findMsgContacts(ownerId);
        List<Object> uidList = contactMsgList.stream().map(c -> c.getUid().toString()).collect(Collectors.toList());
        // 添加会话未读
        List<Object> unreadList = redisService.getHashs(ownerId + CONVERSION_UNREAD_SUFFIX, uidList);
        for (int i = 0; i < contactMsgList.size(); i++) {
            String unreadS = (String) unreadList.get(i);
            Long unread;
            if (StringUtils.isEmpty(unreadS)) {
                unread = 0L;
            } else {
                unread = Long.parseLong(unreadS);
            }
            contactMsgList.get(i).setUnread(unread);
        }
        // 总未读
        Long totalUnRead = queryTotalUnread(ownerId);
        return new MsgContactVO(owner, totalUnRead, contactMsgList);
    }

    public Long queryTotalUnread(Long ownerId) {
        String total = redisService.getString(ownerId + TOTAL_UNREAD_SUFFIX);
        return StringUtils.isEmpty(total) ? 0 : Long.parseLong(total);
    }
}
