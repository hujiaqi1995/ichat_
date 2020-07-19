package com.xdu.ichat.exception;

/**
 * @author hujiaqi
 * @create 2020/6/27
 */
public class UserAlreadyExistException extends RuntimeException {

    public UserAlreadyExistException(String message) {
        super(message);
    }
}
