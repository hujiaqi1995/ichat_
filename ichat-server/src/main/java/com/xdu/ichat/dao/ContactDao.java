package com.xdu.ichat.dao;

import com.xdu.ichat.entity.User;
import com.xdu.ichat.entity.po.ContactPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author hujiaqi
 * @create 2020/6/28
 */

@Mapper
public interface ContactDao {
    List<User> findAll(@Param("ownerUid") Long ownerUid);

    ContactPO findByOwnerUidAndOtherUid(@Param("ownerUid") Long ownerUid, @Param("otherUid") Long otherUid);

    int insert(@Param("ownerUid") Long ownerUid, @Param("otherUid") Long otherUid);
}
