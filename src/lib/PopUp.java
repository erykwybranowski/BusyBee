package lib;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class PopUp {
    private String text;
    private int frames = 60;
    private double sizeMultiplier = 1;
    private int x;
    private int y;

    //text constructor
    public PopUp(String text,int x, int y, double sizeMultiplier) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.sizeMultiplier = sizeMultiplier;
    }

    public String getText() {
        return text;
    }

    public int getFrames() {
        return frames;
    }

    public void reduceFrames() {
        frames--;
    }

    public double getSizeMultiplier() {
        return sizeMultiplier;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
