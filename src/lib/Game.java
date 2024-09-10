package lib;

import lib.animations.Sprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;

public class Game extends JPanel implements Runnable {
    public BufferedImage sheet;
    private Sprite[] grass;
    private Random random;
    private Sprite bee;
    private Sprite[] flowers;
    private Sprite[] hornets;
    private int hornetsCount = 5;
    private boolean hornetsMoved[];
    private final double INITIAL_SPEED = 2;
    private double currentSpeed = INITIAL_SPEED;

    private boolean running = false;

    private final int TARGET_FPS = 60;  // Target frames per second
    private final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;  // Time per frame in nanoseconds

    private int pollenCount = 0;  // Number of times pollen is collected
    private int points = 0;  // Game points

    // Define the number of pollen collections allowed and how much speed to reduce
    private static final int MAX_POLLEN = 3;
    private static final double SPEED_REDUCTION = 0.5;
    private Set<String> pressedKeys = new HashSet<>();
    private final Map<Integer, Boolean> keyStates = new HashMap<>();
    private int heartCount = 3;
    private int grassCount = 300;
    private int flowerCount = 12;

    public Game(CardLayout cardLayout, JPanel cards) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(400, 400));
        setBackground(new Color(81, 121, 71));
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setUpGraphics();

        setUpKeyBindings();

        setFocusable(true);
        requestFocusInWindow();

        setUpButtons(cardLayout, cards);

        startGame();
    }

    private void setUpGraphics() {
        // Load resources
        try {
            URL imageUrl = getClass().getResource("/sheet.png");
            sheet = ImageIO.read(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        random = new Random();

        // Initialize sprites and bee
        Sprite[] tempGrass = new Sprite[grassCount];
        for (int i = 0; i < grassCount; i++) {
            tempGrass[i] = new Sprite(this, random.nextInt(10) + 1, 20 + random.nextInt(36) * 10, 30 + random.nextInt(21) * 16, 500);
        }
        grass = tempGrass;

        bee = new Sprite(this, 11, ((int) getPreferredSize().getWidth() - 17) / 2, ((int) getPreferredSize().getHeight() - 15) / 2, 150);
        Rectangle hiveRectangle = new Rectangle(((int) getPreferredSize().getWidth() - 52) / 2, ((int) getPreferredSize().getHeight() - 58) / 2, 52, 58);

        Sprite[] tempHornets = new Sprite[hornetsCount];
        for (int i = 0; i < hornetsCount; i++) {
            boolean valid = false;
            do {
                Sprite newHornet = new Sprite(this, 17, -100, -100, 300);
                Rectangle newHornetBounds = newHornet.getBounds();

                // Check if the new hornet intersects with the rectangle
                boolean intersectsRectangle = newHornetBounds.intersects(hiveRectangle);

                if (!intersectsRectangle) {
                    valid = true;
                    tempHornets[i] = newHornet;
                }
            } while (!valid);
        }
        hornets = tempHornets;
        hornetsMoved = new boolean[hornetsCount];
        for (boolean hornet : hornetsMoved) {
            hornet = false;
        }

        Sprite[] tempFlowers = new Sprite[flowerCount];

        for (int i = 0; i < flowerCount; i++) {
            boolean valid = false;
            do {
                Sprite newFlower = new Sprite(this, random.nextInt(5) + 12, 20 + random.nextInt(9) * 40, 30 + random.nextInt(5) * 67, 0);
                Rectangle newFlowerBounds = newFlower.getBounds();

                // Check if the new flower intersects with the rectangle
                boolean intersectsRectangle = newFlowerBounds.intersects(hiveRectangle);

                // Check if the new flower intersects with any of the existing flowers
                boolean intersectsOtherFlowers = false;
                for (int j = 0; j < i; j++) {
                    if (newFlowerBounds.intersects(tempFlowers[j].getBounds())) {
                        intersectsOtherFlowers = true;
                        break;
                    }
                }

                if (!intersectsRectangle && !intersectsOtherFlowers) {
                    valid = true;
                    tempFlowers[i] = newFlower;
                }
            } while (!valid);
        }

        flowers = tempFlowers;
    }

    private void setUpButtons(CardLayout cardLayout, JPanel cards) {
        JButton backButton = new JButton("Back to Main Menu");
        backButton.addActionListener(e -> cardLayout.show(cards, "menu"));
        add(backButton, BorderLayout.NORTH);
    }

    private void setUpKeyBindings() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // Arrow Keys Bindings
        inputMap.put(KeyStroke.getKeyStroke("pressed LEFT"), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke("released LEFT"), "stopLeft");
        inputMap.put(KeyStroke.getKeyStroke("pressed RIGHT"), "moveRight");
        inputMap.put(KeyStroke.getKeyStroke("released RIGHT"), "stopRight");
        inputMap.put(KeyStroke.getKeyStroke("pressed UP"), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke("released UP"), "stopUp");
        inputMap.put(KeyStroke.getKeyStroke("pressed DOWN"), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke("released DOWN"), "stopDown");

        actionMap.put("moveLeft", new KeyAction("LEFT", true));
        actionMap.put("stopLeft", new KeyAction("LEFT", false));
        actionMap.put("moveRight", new KeyAction("RIGHT", true));
        actionMap.put("stopRight", new KeyAction("RIGHT", false));
        actionMap.put("moveUp", new KeyAction("UP", true));
        actionMap.put("stopUp", new KeyAction("UP", false));
        actionMap.put("moveDown", new KeyAction("DOWN", true));
        actionMap.put("stopDown", new KeyAction("DOWN", false));

        inputMap.put(KeyStroke.getKeyStroke("shift released LEFT"), "stopLeft");
        inputMap.put(KeyStroke.getKeyStroke("shift released RIGHT"), "stopRight");
        inputMap.put(KeyStroke.getKeyStroke("shift released UP"), "stopUp");
        inputMap.put(KeyStroke.getKeyStroke("shift released DOWN"), "stopDown");
    }

    private void startGame() {
        running = true;
        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    private void updateGame() {
        Rectangle rectangle = new Rectangle(20, 20, 360, 350);
        Rectangle beeBounds = bee.getBounds();

        if (pressedKeys.contains("LEFT") && beeBounds.x > rectangle.x) {
            bee.moveLeft(currentSpeed); }

        if (pressedKeys.contains("RIGHT") && beeBounds.x + beeBounds.width < rectangle.x + rectangle.width) {
            bee.moveRight(currentSpeed); }

        if (pressedKeys.contains("UP") && beeBounds.y > rectangle.y) {
            bee.moveUp(currentSpeed); }

        if (pressedKeys.contains("DOWN") && beeBounds.y + beeBounds.height < rectangle.y + rectangle.height) {
            bee.moveDown(currentSpeed); }

        for (int i = 0; i < hornetsCount; i++) {
            if (!hornetsMoved[i]) {
                if (points >= 100 * (i+1)) {
                    hornets[i].moveRandom();
                    hornetsMoved[i] = true;
                }
            } else {
                // Get bee and hornet positions
                int beeX = bee.getX();
                int beeY = bee.getY();
                int hornetX = hornets[i].getX();
                int hornetY = hornets[i].getY();

// Calculate direction vector from hornet to bee
                int deltaX = beeX - hornetX;
                int deltaY = beeY - hornetY;

// Calculate the distance between the hornet and the bee
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

// Set a movement speed for the hornet
                int hornetSpeed = 2; // Adjust speed as needed

// Normalize the direction vector and multiply by hornetSpeed
                if (distance != 0) {
                    int moveX = (int) (hornetSpeed * (deltaX / distance));
                    int moveY = (int) (hornetSpeed * (deltaY / distance));

                    // Move the hornet toward the bee
                    if (moveX < 0) {
                        hornets[i].moveRight(moveX);  // Move left
                    }
                    if (moveX > 0) {
                        hornets[i].moveLeft(-moveX);  // Move right
                    }
                    if (moveY < 0) {
                        hornets[i].moveDown(moveY);    // Move up
                    }
                    if (moveY > 0) {
                        hornets[i].moveUp(-moveY);   // Move down
                    }
                }

            }
        }

        // Check for collisions with flowers
        checkCollisions();
    }


    private void checkCollisions() {
        // Handle pollen collection from flowers
        for (Sprite flower : flowers) {
            if (bee.getBounds().intersects(flower.getBounds()) && pollenCount < MAX_POLLEN && flower.getCurrentFrameIndex() == 0) {
                pollenCount++;
                currentSpeed = Math.max(0, currentSpeed - SPEED_REDUCTION); // Reduce speed but ensure it's not negative
                flower.collectPollen(this);
            }
        }

        // Handle interaction with beehive
        BufferedImage beehiveImage = sheet.getSubimage(0, 41, 52, 58);
        Rectangle beehiveBounds = new Rectangle(((int) getPreferredSize().getWidth() - beehiveImage.getWidth()) / 2,
                ((int) getPreferredSize().getHeight() - beehiveImage.getHeight()) / 2,
                beehiveImage.getWidth(), beehiveImage.getHeight());

        if (bee.getBounds().intersects(beehiveBounds)) {
            points += pollenCount * pollenCount * 10;  // Add points based on how much pollen was collected
            pollenCount = 0;        // Reset pollen count
            currentSpeed = INITIAL_SPEED;  // Reset speed
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

        for (Sprite sprite: hornets) {
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

        // Draw pollen icons
        BufferedImage pollenIcon = sheet.getSubimage(63,16,17,17);
        for (int i = 0; i < pollenCount; i++) {
            g2d.drawImage(pollenIcon, (int) getPreferredSize().getWidth() + 10, 20 + 20 * i, this);
        }

        BufferedImage heart = sheet.getSubimage(81,16,15,14);
        for (int i = 0; i < heartCount; i++) {
            g2d.drawImage(heart, -25, 20 + 20 * i, this);
        }

        // Draw points over the beehive
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Points: " + points, (int) getPreferredSize().getWidth() / 2 - 20, (int) getPreferredSize().getHeight() / 2 - 60);

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

    private class KeyAction extends AbstractAction {
        private final String key;
        private final boolean pressed;

        public KeyAction(String key, boolean pressed) {
            this.key = key;
            this.pressed = pressed;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (pressed) {
                pressedKeys.add(key);
            } else {
                pressedKeys.remove(key);
            }
        }
    }
}


