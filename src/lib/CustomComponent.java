package lib;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CustomComponent extends JComponent implements MouseListener {
    private BufferedImage backgroundImage;
    private BufferedImage copiedImage;
    private int mouseX, mouseY;

    public CustomComponent() {
        try {
            backgroundImage = ImageIO.read(new File("resources/background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        addMouseListener(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        drawLines(g2d, 100, 100, 500, 100);
        drawText(g2d, "Sample Text", 200, 200);
        drawRectangle(g2d, 300, 300, 200, 100);

        try {
            BufferedImage otherImage = ImageIO.read(new File("resources/moon.jpg"));
            copyImage(otherImage, 329, 81, 142, 142);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (copiedImage != null) {
            drawCopiedImage(g2d, 400, 400);
        }
        if (mouseX != 0 && mouseY != 0) {
            drawCircle(g2d, mouseX, mouseY, 50);
        }
        g2d.dispose();
    }

    public void drawLines(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.setColor(Color.RED);
        g2d.drawLine(x1, y1, x2, y2);
    }

    public void drawText(Graphics2D g2d, String text, int x, int y) {
        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(text, x, y);
    }

    public void drawRectangle(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(Color.GREEN);
        g2d.fillRect(x, y, width, height);
    }

    public void drawCopiedImage(Graphics2D g2d, int x, int y) {
        g2d.drawImage(copiedImage, x, y, null);
    }

    public void drawCircle(Graphics2D g2d, int x, int y, int radius) {
        g2d.setColor(Color.ORANGE);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    public void copyImage(BufferedImage source, int x, int y, int width, int height) {
        copiedImage = source.getSubimage(x, y, width, height);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Eryk Wybranowski");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new CustomComponent());
            frame.setSize(800, 600);
            frame.setVisible(true);
        });
    }
}
