package lib.animations;

import lib.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class Sprite {
    private BufferedImage[] frames;
    private int currentFrameIndex;
    private Timer timer;
    private int x;
    private int y;

    public Sprite(Game gamePanel, int type, int x, int y, int speed) {
        this.x = x;
        this.y = y;
        // Load sprite sheet and define frames
        BufferedImage sheet = gamePanel.sheet;
        switch (type) {
            case 1:
                frames = new BufferedImage[]{
                        sheet.getSubimage(73, 97, 10, 16),
                        sheet.getSubimage(73, 114, 10, 16),
                        sheet.getSubimage(73, 131, 10, 16)
                };
                break;
            case 2:
                frames = new BufferedImage[]{
                        sheet.getSubimage(53, 41, 7, 5),
                        sheet.getSubimage(53, 47, 7, 5)
                };
                break;
            case 3:
                frames = new BufferedImage[]{
                        sheet.getSubimage(80, 49, 8, 15),
                        sheet.getSubimage(80, 65, 8, 15),
                        sheet.getSubimage(80, 81, 8, 15),
                };
                break;
            case 4:
                frames = new BufferedImage[]{
                        sheet.getSubimage(63, 34, 8, 5),
                        sheet.getSubimage(63, 40, 8, 5),
                };
                break;
            case 5:
                frames = new BufferedImage[]{
                        sheet.getSubimage(72, 34, 7, 6),
                        sheet.getSubimage(72, 41, 7, 6),
                        sheet.getSubimage(72, 48, 7, 6),
                };
                break;
            case 6:
                frames = new BufferedImage[]{
                        sheet.getSubimage(73, 55, 5, 6),
                        sheet.getSubimage(73, 62, 5, 6),
                        sheet.getSubimage(73, 69, 5, 6),
                };
                break;
            case 7:
                frames = new BufferedImage[]{
                        sheet.getSubimage(81, 31, 11, 8),
                        sheet.getSubimage(81, 40, 11, 8),
                };
                break;
            case 8:
                frames = new BufferedImage[]{
                        sheet.getSubimage(89, 63, 9, 10),
                        sheet.getSubimage(89, 74, 9, 10),
                        sheet.getSubimage(89, 85, 9, 10),
                };
                break;
            case 9:
                frames = new BufferedImage[]{
                        sheet.getSubimage(99, 63, 7, 8),
                        sheet.getSubimage(99, 72, 7, 8),
                        sheet.getSubimage(99, 81, 7, 8),
                };
                break;
            case 10:
                frames = new BufferedImage[]{
                        sheet.getSubimage(97, 27, 6, 11),
                        sheet.getSubimage(97, 39, 6, 11),
                        sheet.getSubimage(97, 51, 6, 11),
                };
                break;
            case 11: //bee
                frames = new BufferedImage[]{
                        sheet.getSubimage(0, 0, 17,15),//1
                        sheet.getSubimage(18, 0, 17,15),//2
                        sheet.getSubimage(0, 0, 17,15),//1
                        sheet.getSubimage(18, 0, 17,15),//2
                        sheet.getSubimage(36, 0, 17,15),//3
                        sheet.getSubimage(54, 0, 17,15),//4
                        sheet.getSubimage(36, 0, 17,15),//3
                        sheet.getSubimage(54, 0, 17,15),//4
                        sheet.getSubimage(0, 0, 17,15),//1
                        sheet.getSubimage(18, 0, 17,15),//2
                        sheet.getSubimage(0, 0, 17,15),//1
                        sheet.getSubimage(18, 0, 17,15),//2
                        sheet.getSubimage(72, 0, 17,15),//5
                        sheet.getSubimage(90, 0, 17,15),//6
                        sheet.getSubimage(72, 0, 17,15),//5
                        sheet.getSubimage(90, 0, 17,15),//6
                };
                break;
        }

        // Initialize current frame index
        currentFrameIndex = 0;

        // Initialize timer to update frames every 100 milliseconds
        timer = new Timer(speed, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update current frame index
                currentFrameIndex = (currentFrameIndex + 1) % frames.length;
                // Repaint the Game panel to display the new frame
                gamePanel.repaint();
            }
        });
        timer.start();
    }

    public void render(Graphics2D g2d) {
        // Draw current frame onto the provided Graphics2D object
        g2d.drawImage(frames[currentFrameIndex], x, y, null);
    }

    public void moveLeft() {
        this.x -= 1;
        System.out.println(this.x);
    }

    public void moveRight() {
        this.x += 1;
    }

    public void moveUp() {
        this.y -= 1;
    }

    public void moveDown() {
        this.y += 1;
    }
}
