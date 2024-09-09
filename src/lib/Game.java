package lib;

import lib.animations.Sprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;

public class Game extends JPanel implements Runnable {
    public BufferedImage sheet;
    private Sprite[] grass;
    private Random random;
    private Sprite bee;
    private Sprite[] flowers;
    private final double SPEED = 2;

    private boolean running = false;
    private Set<String> pressedKeys;

    private final int TARGET_FPS = 60;  // Target frames per second
    private final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;  // Time per frame in nanoseconds

    public Game(CardLayout cardLayout, JPanel cards) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(400, 400));
        setBackground(new Color(81, 121, 71));
        setLayout(new FlowLayout(FlowLayout.CENTER));

        pressedKeys = new HashSet<>();

        try {
            URL imageUrl = getClass().getResource("/sheet.png");
            sheet = ImageIO.read(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int grassCount = 100;
        Sprite[] sprites = new Sprite[grassCount];
        random = new Random();
        for (int i = 0; i < grassCount; i++) {
            sprites[i] = new Sprite(this, random.nextInt(10) + 1, 20 + random.nextInt(36) * 10, 30 + random.nextInt(21) * 16, 500);
        }
        grass = sprites;

        bee = new Sprite(this, 11, ((int) getPreferredSize().getWidth() - 17) / 2, ((int) getPreferredSize().getHeight() - 15) / 2, 150);

        Sprite[] tempFlowers = new Sprite[10];
        int flowerCount = 10;
        for (int i = 0; i < flowerCount; i++) {
            tempFlowers[i] = new Sprite(this, random.nextInt(5) + 12, 20 + random.nextInt(9) * 40, 30 + random.nextInt(5) * 67, 0);
        }
        flowers = tempFlowers;

        setupKeyBindings();

        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> cardLayout.show(cards, "menu"));
        add(backButton, BorderLayout.NORTH);

        startGame();
    }

    private void setupKeyBindings() {
        // Move Left
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed LEFT"), "moveLeft");
        getActionMap().put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedKeys.add("LEFT");
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released LEFT"), "stopLeft");
        getActionMap().put("stopLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedKeys.remove("LEFT");
            }
        });

        // Move Right
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed RIGHT"), "moveRight");
        getActionMap().put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedKeys.add("RIGHT");
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released RIGHT"), "stopRight");
        getActionMap().put("stopRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedKeys.remove("RIGHT");
            }
        });

        // Move Up
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed UP"), "moveUp");
        getActionMap().put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedKeys.add("UP");
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released UP"), "stopUp");
        getActionMap().put("stopUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedKeys.remove("UP");
            }
        });

        // Move Down
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("pressed DOWN"), "moveDown");
        getActionMap().put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedKeys.add("DOWN");
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released DOWN"), "stopDown");
        getActionMap().put("stopDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pressedKeys.remove("DOWN");
            }
        });
    }

    private void startGame() {
        running = true;
        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    private void updateGame() {
        if (pressedKeys.contains("LEFT")) {
            bee.moveLeft(SPEED);
        }
        if (pressedKeys.contains("RIGHT")) {
            bee.moveRight(SPEED);
        }
        if (pressedKeys.contains("UP")) {
            bee.moveUp(SPEED);
        }
        if (pressedKeys.contains("DOWN")) {
            bee.moveDown(SPEED);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        double scaleY = (double) getHeight() / getPreferredSize().getHeight();

        AffineTransform transform = new AffineTransform();
        transform.translate((getWidth() - getPreferredSize().getWidth() * scaleY) / 2, 0);
        transform.scale(scaleY, scaleY);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.transform(transform);
        g2d.setColor(new Color(89, 160, 74));
        g2d.fillRect(0, 0, (int) getPreferredSize().getWidth(), (int) getPreferredSize().getHeight());

        for (Sprite sprite : grass) {
            sprite.render(g2d);
        }

        for (Sprite sprite : flowers) {
            sprite.render(g2d);
        }

        BufferedImage beehive = sheet.getSubimage(0, 41, 52, 58);
        g2d.drawImage(beehive, ((int) getPreferredSize().getWidth() - beehive.getWidth()) / 2, ((int) getPreferredSize().getHeight() - beehive.getHeight()) / 2, this);

        BufferedImage singleFence = sheet.getSubimage(58, 100, 14, 28);
        BufferedImage horizontalFence = sheet.getSubimage(0, 100, 33, 28);
        BufferedImage verticalFence = sheet.getSubimage(58, 56, 14, 43);
        g2d.drawImage(singleFence, 4, 0, this);
        for (int h = 0; h < 2; h++) {
            for (int i = 0; i < 13; i++) {
                g2d.drawImage(horizontalFence, 14 + 29 * i, h * 368, this);
            }
        }
        for (int h = 0; h < 2; h++) {
            for (int i = 0; i < 16; i++) {
                g2d.drawImage(verticalFence, 4 + h * 377, 8 + 23 * i, this);
            }
        }

        bee.render(g2d);
        g2d.dispose();
    }

    @Override
    public void run() {
        long lastLoopTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;

            // Update the game (movement, logic)
            updateGame();

            // Render the game
            repaint();

            // Sleep for the optimal time to maintain a stable frame rate
            long sleepTime = (lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
