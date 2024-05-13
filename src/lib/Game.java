package lib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.net.URL;

public class Game extends JPanel {
    private BufferedImage beehive;
    private BufferedImage sheet;

    public Game(CardLayout cardLayout, JPanel cards) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(300, 200)); // Set preferred size

        // Load the image
        try {
            URL imageUrl = getClass().getResource("/sheet.png");
            sheet = ImageIO.read(imageUrl);
            // Crop the image
            beehive = sheet.getSubimage(0, 41, 52, 58);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "menu");
            }
        });
        add(backButton, BorderLayout.NORTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background color
        g.setColor(new Color(89, 160, 74)); // #59A04A color
        g.fillRect(0, 0, getWidth(), getHeight());
        // Draw the image
        if (beehive != null) {
            g.drawImage(beehive, 0, 0, this);
        }
    }
}
