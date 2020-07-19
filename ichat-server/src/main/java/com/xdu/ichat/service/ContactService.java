package com.xdu.ichat.service;

import com.xdu.ichat.dao.ContactDao;
import com.xdu.ichat.dao.UserDao;
import com.xdu.ichat.entity.User;
import com.xdu.ichat.entity.po.ContactPO;
import com.xdu.ichat.entity.vo.ContactVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hujiaqi
 * @create 2020/6/29
 */

@Service
public class ContactService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private ContactDao contactDao;

    public boolean add(Long ownerUid, Long otherUid) {
        ContactPO contact = contactDao.findByOwnerUidAndOtherUid(ownerUid, otherUid);
        if (contact == null) {
            contactDao.insert(ownerUid, otherUid);
            return true;
        }
        return false;
    }

    // 查找所有联系人
    public ContactVO queryContacts(Long ownerUid) {
        User owner = userDao.findByUid(ownerUid);
        List<User> contacts = contactDao.findAll(ownerUid);
        return new ContactVO(owner, contacts);
    }

}
