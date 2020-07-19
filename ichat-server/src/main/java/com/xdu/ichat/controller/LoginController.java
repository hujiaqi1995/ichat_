package com.xdu.ichat.controller;

import com.xdu.ichat.common.CommonResult;
import com.xdu.ichat.entity.User;
import com.xdu.ichat.entity.vo.UserVO;
import com.xdu.ichat.entity.Captcha;
import com.xdu.ichat.exception.UserAlreadyExistException;
import com.xdu.ichat.service.RedisService;
import com.xdu.ichat.service.UserService;
import com.xdu.ichat.utils.CaptchaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */

@RestController
@RequestMapping("/user")
@Slf4j
@CrossOrigin
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @GetMapping("/captcha_id")
    public CommonResult<?> getCaptchaId() {
        String captchaId = UUID.randomUUID().toString();
        String text = CaptchaUtil.generateText();
        redisService.setHash("captcha", captchaId, text);
        return CommonResult.success(captchaId);
    }

    @GetMapping("/captcha")
    public void verifyCode(String captchaId, HttpServletResponse response) {
        try {
            String text = redisService.getHash("captcha", captchaId);
            Captcha captcha = CaptchaUtil.getCaptcha(text);
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/jpeg");
            CaptchaUtil.output(captcha.getImage(), response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败, {}", e.getMessage());
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public CommonResult<String> register(@RequestBody UserVO userVO) {
        if (StringUtils.isEmpty(userVO.getNick())
                || StringUtils.isEmpty(userVO.getUsername())
                || StringUtils.isEmpty(userVO.getPassword())) {
            return CommonResult.failed("昵称，用户名，和密码不能为空!");
        }
        try {
            userService.register(userVO.getNick(), userVO.getUsername(), userVO.getPassword());
        } catch (UserAlreadyExistException e) {
            return CommonResult.failed("用户名已存在!");
        }
        return CommonResult.success("");
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public CommonResult<?> login(UserVO userVO, HttpServletRequest request, HttpServletResponse response) {

        log.info("userVO" + userVO.toString());
        String text = redisService.getHash("captcha", userVO.getCaptchaId());
        log.info("验证码"+text);
        if (StringUtils.isEmpty(userVO.getUsername())
                || StringUtils.isEmpty(userVO.getPassword())) {
            return CommonResult.failed("用户名或密码不能为空!");
        }
        if (StringUtils.isEmpty(userVO.getCaptcha())) {
            return CommonResult.failed("验证码不能为空!");
        }

        if (StringUtils.isEmpty(text)
                || !userVO.getCaptcha().toLowerCase().equals(text.toLowerCase())) {
            return CommonResult.failed("验证码错误!");
        }
        try {
            String token = userService.login(userVO.getUsername(), userVO.getPassword());
            User user = userService.findByUsername(userVO.getUsername());
            log.info("用户{}登录成功!", user.getNick());
            request.getSession().setAttribute("uid", user.getUid());
            request.setAttribute("Authorization", "Bearer " + token);
            request.getSession().removeAttribute("captcha");
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("owner", user);
            return CommonResult.success(map);
        } catch (BadCredentialsException e) {
            log.error("用户名或者密码错误");
            return CommonResult.failed("用户名或者密码错误!");
        }
    }
}
