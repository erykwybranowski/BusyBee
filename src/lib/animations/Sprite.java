package lib.animations;

import lib.Game;
import lib.SoundManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Sprite {
    public int framesRemaining = 0;
    public double directionX;
    public double directionY;
    private BufferedImage[] frames;
    private int currentFrameIndex;
    private Timer timer;
    private double x;
    private double y;
    private boolean mirrored = false;
    private int width = 0;
    private int height = 0;
    private final int flowerTime = 20000;
    private SoundManager soundManager;

    public Sprite(JPanel gamePanel, BufferedImage sheet, int type, int x, int y, int speed) {
        this.x = x;
        this.y = y;
        // Load sprite sheet and define frames
        switch (type) {
            case 1: //grass 1
                frames = new BufferedImage[]{
                        sheet.getSubimage(73, 97, 10, 16),
                        sheet.getSubimage(73, 114, 10, 16),
                        sheet.getSubimage(73, 131, 10, 16)
                };
                break;
            case 2: //grass 2
                frames = new BufferedImage[]{
                        sheet.getSubimage(53, 41, 7, 5),
                        sheet.getSubimage(53, 47, 7, 5)
                };
                break;
            case 3: //grass 3
                frames = new BufferedImage[]{
                        sheet.getSubimage(80, 49, 8, 15),
                        sheet.getSubimage(80, 65, 8, 15),
                        sheet.getSubimage(80, 81, 8, 15),
                };
                break;
            case 4: //grass 4
                frames = new BufferedImage[]{
                        sheet.getSubimage(63, 34, 8, 5),
                        sheet.getSubimage(63, 40, 8, 5),
                };
                break;
            case 5: //grass 5
                frames = new BufferedImage[]{
                        sheet.getSubimage(72, 34, 7, 6),
                        sheet.getSubimage(72, 41, 7, 6),
                        sheet.getSubimage(72, 48, 7, 6),
                };
                break;
            case 6: //grass 6
                frames = new BufferedImage[]{
                        sheet.getSubimage(73, 55, 5, 6),
                        sheet.getSubimage(73, 62, 5, 6),
                        sheet.getSubimage(73, 69, 5, 6),
                };
                break;
            case 7: //grass 7
                frames = new BufferedImage[]{
                        sheet.getSubimage(81, 31, 11, 8),
                        sheet.getSubimage(81, 40, 11, 8),
                };
                break;
            case 8: //grass 8
                frames = new BufferedImage[]{
                        sheet.getSubimage(89, 63, 9, 10),
                        sheet.getSubimage(89, 74, 9, 10),
                        sheet.getSubimage(89, 85, 9, 10),
                };
                break;
            case 9: //grass 9
                frames = new BufferedImage[]{
                        sheet.getSubimage(99, 63, 7, 8),
                        sheet.getSubimage(99, 72, 7, 8),
                        sheet.getSubimage(99, 81, 7, 8),
                };
                break;
            case 10: //grass 10
                frames = new BufferedImage[]{
                        sheet.getSubimage(97, 27, 6, 11),
                        sheet.getSubimage(97, 39, 6, 11),
                        sheet.getSubimage(97, 51, 6, 11),
                };
                break;
            case 11: //bee
                width = 17;
                height = 15;
                frames = new BufferedImage[]{
                        sheet.getSubimage(0, 0, 17, 15),//1
                        sheet.getSubimage(18, 0, 17, 15),//2
                        sheet.getSubimage(0, 0, 17, 15),//1
                        sheet.getSubimage(18, 0, 17, 15),//2
                        sheet.getSubimage(36, 0, 17, 15),//3
                        sheet.getSubimage(54, 0, 17, 15),//4
                        sheet.getSubimage(36, 0, 17, 15),//3
                        sheet.getSubimage(54, 0, 17, 15),//4
                        sheet.getSubimage(0, 0, 17, 15),//1
                        sheet.getSubimage(18, 0, 17, 15),//2
                        sheet.getSubimage(0, 0, 17, 15),//1
                        sheet.getSubimage(18, 0, 17, 15),//2
                        sheet.getSubimage(72, 0, 17, 15),//5
                        sheet.getSubimage(90, 0, 17, 15),//6
                        sheet.getSubimage(72, 0, 17, 15),//5
                        sheet.getSubimage(90, 0, 17, 15),//6
                };
                break;
            case 12: //blue flower
                width = 19;
                height = 29;
                frames = new BufferedImage[]{
                        sheet.getSubimage(111, 1, 19, 29),
                        sheet.getSubimage(131, 1, 19, 29)
                };
                break;
            case 13: //yellow flower
                width = 19;
                height = 29;
                frames = new BufferedImage[]{
                        sheet.getSubimage(111, 31, 19, 29),
                        sheet.getSubimage(131, 31, 19, 29)
                };
                break;
            case 14: //red flower
                width = 19;
                height = 29;
                frames = new BufferedImage[]{
                        sheet.getSubimage(111, 61, 19, 29),
                        sheet.getSubimage(131, 61, 19, 29)
                };
                break;
            case 15: //purple flower
                width = 19;
                height = 29;
                frames = new BufferedImage[]{
                        sheet.getSubimage(111, 91, 19, 29),
                        sheet.getSubimage(131, 91, 19, 29)
                };
                break;
            case 16: //white flower
                width = 19;
                height = 29;
                frames = new BufferedImage[]{
                        sheet.getSubimage(111, 121, 19, 29),
                        sheet.getSubimage(131, 121, 19, 29)
                };
                break;
            case 17: //hornet
                width = 31;
                height = 24;
                frames = new BufferedImage[]{
                        sheet.getSubimage(0, 16, width, height),
                        sheet.getSubimage(32, 16, width, height)
                };
                break;
            case 18: //heart
                width = 15;
                height = 14;
                frames = new BufferedImage[]{
                        sheet.getSubimage(81, 16, width, height)
                };
                break;
            case 19: //combo
                width = 13;
                height = 13;
                frames = new BufferedImage[]{
                        sheet.getSubimage(51, 137, width, height)
                };
                break;
        }

        // Initialize current frame index
        currentFrameIndex = 0;

        // Initialize timer to update frames
        if (type == 17 || type == 11) {
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
        } else if (speed == 0) {
            soundManager = new SoundManager();
            timer = new Timer(flowerTime, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (currentFrameIndex == 1) {
                        currentFrameIndex = 0;
                        soundManager.playSound("/pollen_spawn.wav", false, 1);
                        gamePanel.repaint();
                    }
                }
            });
            timer.start();
        } else if (speed == -1) {
            double amplitude = 0.5;  // The maximum vertical distance from the central position
            double frequency = 0.1; // How fast the oscillation happens
            final double[] time = {0};  // Time counter to increment over each frame

            timer = new Timer(30, new ActionListener() {  // Timer triggers every 30ms
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Calculate the new vertical offset using the sine function
                    double offsetY = amplitude * Math.sin(frequency * time[0]);

                    // Use the moveUp method with the calculated offset
                    moveUp(offsetY);  // Move the sprite up and down based on sine wave

                    // Increment time for the next frame
                    time[0] += 1;

                    // Repaint the game panel to show the new position
                    gamePanel.repaint();
                }
            });
            timer.start();  // Start the timer
        }
    }

    public void render(Graphics2D g2d) {
        BufferedImage img = frames[currentFrameIndex];
        if (mirrored) {
            img = flipImage(img);
        }
        g2d.drawImage(img, (int) x, (int) y, null);
    }

    private BufferedImage flipImage(BufferedImage image) {

        // Create a new buffered image with the same dimensions and type as the original
        BufferedImage flippedImage = new BufferedImage(width, height, image.getType());

        // Create an AffineTransform to flip the image horizontally
        AffineTransform tx = new AffineTransform();

        tx.scale(-1, 1); // Flip horizontally
        tx.translate(-width, 0); // Translate to adjust position after flipping


        // Draw the original image onto the flipped image using the AffineTransform
        Graphics2D g2d = flippedImage.createGraphics();
        g2d.drawImage(image, tx, null);
        g2d.dispose();

        return flippedImage;
    }

    public void moveLeft(double speed) {
        this.mirrored = true;
        this.x -= speed;
    }

    public void moveRight(double speed) {
        this.mirrored = false;
        this.x += speed;
    }

    public void moveUp(double speed) {
        this.y -= speed;
    }

    public void moveDown(double speed) {
        this.y += speed;
    }

    public void collectPollen(Game gamePanel) {
        this.currentFrameIndex = 1;
        gamePanel.repaint();
        timer.restart();
    }

    public int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    public void setCurrentFrameIndex(int value) {
        currentFrameIndex = value;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public void moveRandom(Sprite[][] omitList) {
        Random random = new Random();
        boolean valid = false;
        int x;
        int y;
        do {
            x = 20 + random.nextInt(9) * 40;
            y = 30 + random.nextInt(5) * 67;
            Rectangle hiveRectangle = new Rectangle(174, 171, 52, 58);
            Rectangle spriteRectangle = new Rectangle(x, y, width, height);
            boolean intersectsHive = hiveRectangle.intersects(spriteRectangle);
            boolean intersectsSprite = false;
            for (Sprite[] spriteType : omitList) {
                for (Sprite sprite : spriteType) {
                    if (sprite.getBounds().intersects(spriteRectangle)) {
                        intersectsSprite = true;
                    }
                }
            }
            if (!intersectsHive && !intersectsSprite) {
                valid = true;
            }
        } while (!valid);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void stopAnimation() {
        if (timer != null) {
            timer.stop();  // Stop the animation timer
        }
    }

    public int getFramesLength() {
        return frames.length;
    }

    public void moveHorizontally(double directionValue, double speedValue) {
        if (directionValue < 0) {
            this.moveRight(-speedValue);
        }
        if (directionValue > 0) {
            this.moveLeft(-speedValue);
        }
    }

    public void moveVertically(double directionValue, double speedValue) {
        if (directionValue < 0) {
            this.moveUp(speedValue);
        }
        if (directionValue > 0) {
            this.moveDown(speedValue);
        }
    }
}
