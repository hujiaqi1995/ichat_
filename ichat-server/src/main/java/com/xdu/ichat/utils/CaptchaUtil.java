package com.xdu.ichat.utils;

import com.xdu.ichat.entity.Captcha;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author hujiaqi
 * @create 2020/6/26
 */

public class CaptchaUtil {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 30;
    private static final String[] fontNames = {"宋体", "楷体", "隶书", "微软雅黑"};
    private static final Color bgColor = new Color(255, 255, 255);
    private static final String codes = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LEN = 62;
    private static final int FONT_NAME_LEN = fontNames.length;
    private static final int TEXT_LEN = 4;

    public static Captcha getCaptcha() {
        String text = generateText();
        BufferedImage image = generateImage(text);
        return new Captcha(text, image);
    }

    public static Captcha getCaptcha(String text) {
        BufferedImage image = generateImage(text);
        return new Captcha(text, image);
    }

    public static void output(BufferedImage image, OutputStream out) throws IOException {
        ImageIO.write(image, "JPEG", out);
    }

    public static String generateText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TEXT_LEN; i++) {
            char ch = codes.charAt(ThreadLocalRandom.current().nextInt(CODE_LEN));
            sb.append(ch);
        }
        return sb.toString();
    }

    private static BufferedImage generateImage(String text) {
        BufferedImage image = createImage();
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        for (int i = 0; i < TEXT_LEN; i++) {
            g2.setFont(randomFont());
            g2.setColor(randomColor());
            float x = i * WIDTH * 1.0f / 4;
            g2.drawString(String.valueOf(text.charAt(i)), x, HEIGHT - 8);
        }
        drawLine(image);
        return image;
    }

    private static Color randomColor() {
        int red = ThreadLocalRandom.current().nextInt(150);
        int green = ThreadLocalRandom.current().nextInt(150);
        int blue = ThreadLocalRandom.current().nextInt(150);
        return new Color(red, green, blue);
    }

    private static Font randomFont() {
        String name = fontNames[ThreadLocalRandom.current().nextInt(FONT_NAME_LEN)];
        int style = ThreadLocalRandom.current().nextInt(FONT_NAME_LEN);
        int size = ThreadLocalRandom.current().nextInt(5) + 24;
        return new Font(name, style, size);
    }

    private static BufferedImage createImage() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setColor(bgColor);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        return image;
    }

    private static void drawLine(BufferedImage image) {
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        int num = 5;
        for (int i = 0; i < num; i++) {
            int x1 = ThreadLocalRandom.current().nextInt(WIDTH);
            int y1 = ThreadLocalRandom.current().nextInt(HEIGHT);
            int x2 = ThreadLocalRandom.current().nextInt(WIDTH);
            int y2 = ThreadLocalRandom.current().nextInt(HEIGHT);
            g2.setColor(randomColor());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x1, y1, x2, y2);
        }
    }
}
