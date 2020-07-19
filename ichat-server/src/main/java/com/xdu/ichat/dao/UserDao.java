package com.xdu.ichat.dao;


import com.xdu.ichat.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author hujiaqi
 * @create 2020/6/27
 */

@Mapper
public interface UserDao {
    User findByUsername(String username);

    User findByUid(Long uid);

    int insert(User user);
}
