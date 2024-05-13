package lib;

import lib.animations.Grass1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLOutput;
import javax.imageio.ImageIO;

public class Game extends JPanel {
    private BufferedImage sheet;

    public Game(CardLayout cardLayout, JPanel cards) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(400, 400)); // Set preferred size to 400x400
        setBackground(new Color(81, 121, 71));
        setLayout(new FlowLayout(FlowLayout.CENTER));


        // Load the image
        try {
            URL imageUrl = getClass().getResource("/sheet.png");
            sheet = ImageIO.read(imageUrl);
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

        // Calculate the scale factor based on the size of the panel
        double scaleY = (double) getHeight() / getPreferredSize().getHeight();

        AffineTransform transform = new AffineTransform();
        transform.translate((getWidth()-getPreferredSize().getWidth()*scaleY)/2,0);
        transform.scale(scaleY, scaleY);
        Graphics2D g2d = (Graphics2D) g.create();

        // Draw the background color
        g2d.transform(transform);
        g2d.setColor(new Color(89, 160, 74)); // #59A04A color
        g2d.fillRect(0, 0, (int)getPreferredSize().getWidth(), (int)getPreferredSize().getHeight());

        // Draw the scene
        BufferedImage beehive = sheet.getSubimage(0, 41, 52, 58);
        g2d.drawImage(beehive, ((int)getPreferredSize().getWidth() - beehive.getWidth()) / 2, ((int)getPreferredSize().getHeight() - beehive.getHeight()) / 2, this);

        BufferedImage singleFence = sheet.getSubimage(58,100,14,28);
        BufferedImage horizontalFence = sheet.getSubimage(0,100,33,28);
        BufferedImage verticalFence = sheet.getSubimage(58,56,14,43);
        g2d.drawImage(singleFence,4,0,this);
        for(int h=0; h<2; h++){
            for(int i=0; i<13; i++){
                g2d.drawImage(horizontalFence,14+29*i,h*368,this);
            }
        }
        for(int h=0; h<2; h++){
            for(int i=0; i<16; i++){
                g2d.drawImage(verticalFence, 4+h*377,8+23*i,this);
            }
        }

        Grass1 grass1 = new Grass1();
        grass1.render(g2d, 50, 50);
        grass1.startAnimation();
        g2d.dispose();
    }
}
