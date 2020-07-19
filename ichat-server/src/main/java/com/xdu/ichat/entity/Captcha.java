package com.xdu.ichat.entity;

import java.awt.image.BufferedImage;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */
public class Captcha {
    private String text;
    private BufferedImage image;

    public Captcha(String text, BufferedImage image) {
        this.text = text;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
