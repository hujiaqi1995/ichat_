CREATE TABLE if NOT EXISTS ichat_user
(
    uid         BIGINT(20) UNSIGNED AUTO_INCREMENT,
    nick        VARCHAR(64) NOT NULL,
    username    VARCHAR(64) NOT NULL,
    password    VARCHAR(64) NOT NULL,
    avatar      VARCHAR(64) DEFAULT NULL,
    create_time TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (uid),
    UNIQUE KEY IDX_USERNAME (username)
);

CREATE TABLE IF NOT EXISTS ichat_msg_content
(
    mid     BIGINT(20) UNSIGNED AUTO_INCREMENT,
    content VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (mid)
);

CREATE TABLE IF NOT EXISTS ichat_msg_relation
(
    owner_uid   BIGINT(20) UNSIGNED NOT NULL,
    other_uid   BIGINT(20) UNSIGNED NOT NULL,
    mid         BIGINT(20) UNSIGNED NOT NULL,
    type        TINYINT(1)          NOT NULL,
    create_time TIMESTAMP           NOT NULL,
    PRIMARY KEY (`owner_uid`, `mid`),
    KEY idx_owneruid_otheruid_msgid (`owner_uid`, `other_uid`, `mid`)
);

CREATE TABLE IF NOT EXISTS ichat_msg_contact
(
    owner_uid   BIGINT(20) UNSIGNED NOT NULL,
    other_uid   BIGINT(20) UNSIGNED NOT NULL,
    mid         BIGINT(20) UNSIGNED NOT NULL,
    type        TINYINT(1)          NOT NULL,
    create_time TIMESTAMP           NOT NULL,
    PRIMARY KEY (`owner_uid`, `other_uid`)
);

CREATE TABLE IF NOT EXISTS ichat_contact
(
    owner_uid     BIGINT(20) UNSIGNED NOT NULL,
    other_uid     BIGINT(20) UNSIGNED NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (owner_uid, other_uid)
);