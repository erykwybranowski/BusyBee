package lib;

import lib.animations.Sprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import javax.imageio.ImageIO;

public class Game extends JPanel {
    public BufferedImage sheet;
    private Sprite[] grass;
    private Random random;
    private Sprite bee;

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
        Sprite[] sprites = new Sprite[100];
        random = new Random();
        for(int i=0; i<100; i++){
            sprites[i] = new Sprite(this,random.nextInt(10)+1, 20+random.nextInt(36)*10,30+random.nextInt(21)*16, 500);
        }
        grass = sprites;

        bee = new Sprite(this,11,((int)getPreferredSize().getWidth()-17)/2,((int)getPreferredSize().getHeight()-15)/2, 100);
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Handle arrow key events
                int keyCode = e.getKeyCode();
                System.out.println(keyCode);
                switch (keyCode) {
                    case KeyEvent.VK_LEFT:
                        bee.moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        bee.moveRight();
                        break;
                    case KeyEvent.VK_UP:
                        bee.moveUp();
                        break;
                    case KeyEvent.VK_DOWN:
                        bee.moveDown();
                        break;
                }
                repaint(); // Repaint the panel after moving the grass object
            }
        });

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
        // Render the animated object (grass1) onto the panel
        for(int i=0; i<grass.length; i++){
            grass[i].render(g2d);
        }

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

        bee.render(g2d);
        g2d.dispose();
    }
}
