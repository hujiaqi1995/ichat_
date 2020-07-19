package com.xdu.ichat.service;

import com.xdu.ichat.dao.UserDao;
import com.xdu.ichat.entity.AdminUserDetails;
import com.xdu.ichat.entity.User;
import com.xdu.ichat.exception.UserAlreadyExistException;
import com.xdu.ichat.jwt.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author hujiaqi
 * @create 2020/6/27
 */

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    public void register(String nick, String username, String password) {
        User user0 = userDao.findByUsername(username);
        if (user0 != null) {
            throw new UserAlreadyExistException("邮箱已被注册");
        }
        User user = new User();
        user.setNick(nick);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userDao.insert(user);
    }

    public String login(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("密码不正确");
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenUtil.generateToken(userDetails);
    }

    public User findByUid(Long uid) {
        return userDao.findByUid(uid);
    }

    public User findByUsername(String username){
        return userDao.findByUsername(username);
    }

    public void addContact(Long ownerUid, Long otherUid) {

    }
}
