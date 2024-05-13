package lib.animations;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class Grass1 extends JComponent implements ActionListener {
    private BufferedImage[] frames;
    private int currentFrameIndex;
    private Timer timer;

    public Grass1() {
        // Load sprite sheet and define frames
        try {
            URL imageUrl = getClass().getResource("/sheet.png");
            BufferedImage sheet = ImageIO.read(imageUrl);
            frames = new BufferedImage[]{
                    sheet.getSubimage(73, 97, 10, 16),
                    sheet.getSubimage(73, 114, 10, 16),
                    sheet.getSubimage(73, 131, 10, 16),
            };
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize current frame index
        currentFrameIndex = 0;

        // Initialize timer to update frames every second
        timer = new Timer(1000, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw current frame
        g.drawImage(frames[currentFrameIndex], getX(), getY(), null);
//        System.out.println(frames.length);

    }

    public void render(Graphics2D g2d, int x, int y) {
        // Draw current frame onto the provided Graphics2D object at position (x, y)
        g2d.drawImage(frames[currentFrameIndex], x, y, null);
//        System.out.println(currentFrameIndex);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(currentFrameIndex);
        // Update current frame index
        currentFrameIndex = (currentFrameIndex + 1) % frames.length;
        // Repaint the component to display the new frame
        repaint();
    }
}