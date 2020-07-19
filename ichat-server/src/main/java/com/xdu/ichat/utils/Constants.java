package com.xdu.ichat.utils;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */
public class Constants {
    public static final String CAPTCHA_HASH_KEY = "CAPTCHA_HASH_KEY";

    public static final String CAPTCHA_ID = "CAPTCHA_ID";

    public static final String WEBSOCKET_MSG_TOPIC = "websocket:msg";

    public static final String TOTAL_UNREAD_SUFFIX = "_T";

    public static final String CONVERSION_UNREAD_SUFFIX = "_C";

    public static final int MSG_SEND = 0;

    public static final int MSG_RECEIVE = 1;

    public static final Long JWT_EXPIRE_TIME = 8 * 60 * 60 * 3600L;

    public static final int IM_HEARTBEAT = 0;

    public static final int IM_ONLINE = 1;

    public static final int IM_QUERY_MESSAGE = 2;

    public static final int IM_SEND_MESSAGE = 3;

    public static final int IM_QUERY_TOTAL_UNREAD = 5;

    public static final int IM_CLIENT_ACK = 6;

}
