package com.xdu.ichat.dao;

import com.xdu.ichat.entity.po.MsgContactPO;
import com.xdu.ichat.entity.po.MsgContentPO;
import com.xdu.ichat.entity.po.MsgPO;
import com.xdu.ichat.entity.po.MsgRelationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hujiaqi
 * @create 2020/6/28
 */

@Mapper
public interface MsgDao {

    List<MsgPO> findAll(@Param("ownerUid") long ownerUid, @Param("otherUid") long otherUid);

    List<MsgContactPO> findMsgContacts(@Param("ownerUid") Long ownerUid);

    MsgRelationPO findMsgContact(@Param("ownerUid") Long ownerUid, @Param("otherUid") Long otherUid);

    int insertMsgContent(MsgContentPO msgContentPO);

    int insertMsgRelation(MsgRelationPO msgRelationPO);

    int insertMsgContact(MsgRelationPO msgRelationPO);

    int updateMsgContact(MsgRelationPO msgRelationPO);
}
