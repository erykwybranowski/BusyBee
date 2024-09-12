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
    private final CardLayout cardLayout;
    private final JPanel cards;
    Thread gameThread;
    public BufferedImage sheet;
    private Sprite[] grass;
    private Random random;
    private Sprite bee;
    private Sprite[] flowers;
    private Sprite[] hornets;
    private int hornetsCount = 5;
    private boolean hornetsMoved[];
    private final double INITIAL_SPEED = 2;
    private static final double SPEED_REDUCTION = 0.4;
    private double currentSpeed = INITIAL_SPEED;
    private boolean running = false;
    private final int TARGET_FPS = 60;  // Target frames per second
    private final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;  // Time per frame in nanoseconds
    private int pollenCount = 0;  // Number of times pollen is collected
    private int points = 0;  // Game points
    // Define the number of pollen collections allowed and how much speed to reduce
    private static final int MAX_POLLEN = 3;
    private Set<String> pressedKeys = new HashSet<>();
    private int heartCount = 3;
    private int grassCount = 300;
    private int flowerCount = 20;
    private double hornetSpeed = 0.6;
    private int framesToChangeHornetDirection = 120;

    public Game(CardLayout cardLayout, JPanel cards) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(400, 400));
        setBackground(new Color(81, 121, 71));
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setUpKeyBindings();
        setFocusable(true);
        requestFocusInWindow();
        this.cardLayout = cardLayout;
        this.cards = cards;
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
            tempHornets[i] = new Sprite(this, 17, -100, -100, 300);
        }
        hornets = tempHornets;
        hornetsMoved = new boolean[hornetsCount];

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
        ImageIcon backButtonIcon = new ImageIcon(sheet.getSubimage(0, 0, 17, 15));  // Coordinates of the "Back" icon
        // Scale the icon using AffineTransform (similar to g2d scaling)
        Image scaledBackButtonImage = backButtonIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        ImageIcon scaledBackButtonIcon = new ImageIcon(scaledBackButtonImage);

        // Create the button with the scaled icon
        JButton backButton = new JButton(scaledBackButtonIcon);
        backButton.setBorderPainted(false);  // Remove button borders
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);  // Make button background transparent

        // Set position manually (absolute positioning)
        setLayout(null);  // Disable layout manager
        backButton.setBounds(0, 20, 100, 100);  // Position it similarly to the hearts (-25 isn't needed for components)

        // Add action listener for the button
        backButton.addActionListener(e -> {
            stopGame();  // Stop the game when going back to the main menu
            cardLayout.show(cards, "menu");
        });

        add(backButton);
    }

    private void setUpKeyBindings() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // Arrow Keys Bindings
        inputMap.put(KeyStroke.getKeyStroke("pressed LEFT"), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke("pressed A"), "moveLeft");
        inputMap.put(KeyStroke.getKeyStroke("released LEFT"), "stopLeft");
        inputMap.put(KeyStroke.getKeyStroke("released A"), "stopLeft");
        inputMap.put(KeyStroke.getKeyStroke("pressed RIGHT"), "moveRight");
        inputMap.put(KeyStroke.getKeyStroke("pressed D"), "moveRight");
        inputMap.put(KeyStroke.getKeyStroke("released RIGHT"), "stopRight");
        inputMap.put(KeyStroke.getKeyStroke("released D"), "stopRight");
        inputMap.put(KeyStroke.getKeyStroke("pressed UP"), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke("pressed W"), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke("released UP"), "stopUp");
        inputMap.put(KeyStroke.getKeyStroke("released W"), "stopUp");
        inputMap.put(KeyStroke.getKeyStroke("pressed DOWN"), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke("pressed S"), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke("released DOWN"), "stopDown");
        inputMap.put(KeyStroke.getKeyStroke("released S"), "stopDown");

        actionMap.put("moveLeft", new KeyAction("LEFT", true));
        actionMap.put("stopLeft", new KeyAction("LEFT", false));
        actionMap.put("moveRight", new KeyAction("RIGHT", true));
        actionMap.put("stopRight", new KeyAction("RIGHT", false));
        actionMap.put("moveUp", new KeyAction("UP", true));
        actionMap.put("stopUp", new KeyAction("UP", false));
        actionMap.put("moveDown", new KeyAction("DOWN", true));
        actionMap.put("stopDown", new KeyAction("DOWN", false));

        inputMap.put(KeyStroke.getKeyStroke("shift released LEFT"), "stopLeft");
        inputMap.put(KeyStroke.getKeyStroke("shift released A"), "stopLeft");
        inputMap.put(KeyStroke.getKeyStroke("shift released RIGHT"), "stopRight");
        inputMap.put(KeyStroke.getKeyStroke("shift released D"), "stopRight");
        inputMap.put(KeyStroke.getKeyStroke("shift released UP"), "stopUp");
        inputMap.put(KeyStroke.getKeyStroke("shift released W"), "stopUp");
        inputMap.put(KeyStroke.getKeyStroke("shift released DOWN"), "stopDown");
        inputMap.put(KeyStroke.getKeyStroke("shift released S"), "stopDown");
    }

    public void startGame() {
        if (gameThread == null || !gameThread.isAlive()) {
            resetGame();  // Reset the game state before starting
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
            setUpButtons(cardLayout, cards);
        }
    }

    public void stopGame() {
        running = false;

        bee.stopAnimation();
        Sprite[][] sprites = {grass, flowers, hornets};
        for (Sprite[] spriteArray : sprites) {
            for (Sprite sprite : spriteArray) {
                sprite.stopAnimation();
            }
        }

        try {
            if (gameThread != null && gameThread.isAlive()) {
                gameThread.join();  // Wait for the game thread to fully stop
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gameThread = null;  // Reset the thread to null so it can be restarted
    }

    private void resetGame() {
        setUpGraphics();  // Reload game graphics if needed
//        points = 0;  // Reset score
//        pollenCount = 0;  // Reset pollen count
//        heartCount = 3;  // Reset hearts/lives
        pressedKeys.clear();  // Reset pressed keys
        currentSpeed = INITIAL_SPEED;  // Reset speed
    }

    private void showGameOverScreen() {
        //GameOverDialog dialog = new GameOverDialog((JFrame) SwingUtilities.getWindowAncestor(this), cardLayout, cards, this);
        //dialog.setVisible(true);  // This will show the modal dialog and pause the game thread until it's closed
    }


    private void updateGame() {
        Rectangle gameArea = new Rectangle(20, 20, 360, 350);
        Rectangle beeBounds = bee.getBounds();

        if (pressedKeys.contains("LEFT") && beeBounds.x > gameArea.x) {
            bee.moveLeft(currentSpeed); }

        if (pressedKeys.contains("RIGHT") && beeBounds.x + beeBounds.width < gameArea.x + gameArea.width) {
            bee.moveRight(currentSpeed); }

        if (pressedKeys.contains("UP") && beeBounds.y > gameArea.y) {
            bee.moveUp(currentSpeed); }

        if (pressedKeys.contains("DOWN") && beeBounds.y + beeBounds.height < gameArea.y + gameArea.height) {
            bee.moveDown(currentSpeed); }

        defineHornetsMovement(gameArea);
        // Check for collisions with flowers
        checkFlowersCollisions();
        checkHornetsCollisions();
    }

    private void checkHornetsCollisions() {
        for (int i = 0; i < hornetsCount; i++) {
            if (hornets[i].getBounds().intersects(bee.getBounds())) {
                pollenCount = 0;
                heartCount--;
                bee.setX((int) (getPreferredSize().getWidth()/2) - bee.getWidth()/2);
                bee.setY((int) (getPreferredSize().getHeight()/2) - bee.getHeight()/2);
                if (heartCount == 0) {
                    running = false;
                    showGameOverScreen();
                }
            }
        }
    }

    private void defineHornetsMovement(Rectangle gameArea) {
        Rectangle hiveRectangle = new Rectangle(((int) getPreferredSize().getWidth() - 52) / 2, ((int) getPreferredSize().getHeight() - 58) / 2, 52, 58);

        for (int i = 0; i < hornetsCount; i++) {
            if (!hornetsMoved[i]) {
                if (points >= 100 * (i + 1)) {
                    hornets[i].moveRandom();
                    hornetsMoved[i] = true;
                }
            } else if (!bee.getBounds().intersects(hiveRectangle.getBounds())) {
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

                // Normalize the direction vector and multiply by hornetSpeed
                if (distance != 0) {
                    double moveX = (hornetSpeed * (deltaX / distance));
                    double moveY = (hornetSpeed * (deltaY / distance));

                    Rectangle hornetMovedX = new Rectangle(hornetX + roundAwayFromZero(moveX), hornetY, hornets[i].getWidth(), hornets[i].getHeight());
                    Rectangle hornetMovedY = new Rectangle(hornetX, hornetY + roundAwayFromZero(moveY), hornets[i].getWidth(), hornets[i].getHeight());

                    if (!hornetMovedX.intersects(hiveRectangle)) {
                        if (moveX < 0) {
                            hornets[i].moveRight(moveX);
                        }
                        if (moveX > 0) {
                            hornets[i].moveLeft(-moveX);
                        }
                    }

                    if (!hornetMovedY.intersects(hiveRectangle)) {
                        if (moveY < 0) {
                            hornets[i].moveUp(-moveY);
                        }
                        if (moveY > 0) {
                            hornets[i].moveDown(moveY);
                        }
                    }
                }
            } else {
                // HERE - Random movement when hornet is near the hive

                if (hornets[i].framesRemaining <= 0) {
                    // Choose a new random direction
                    double angle = Math.random() * 2 * Math.PI; // Random angle in radians
                    hornets[i].directionX = Math.cos(angle);
                    hornets[i].directionY = Math.sin(angle);
                    hornets[i].framesRemaining = framesToChangeHornetDirection; // Move in this direction for 60 frames (1 second)
                } else {
                    // Move in the current random direction
                    double moveX = hornetSpeed * hornets[i].directionX;
                    double moveY = hornetSpeed * hornets[i].directionY;
                    int hornetX = hornets[i].getX();
                    int hornetY = hornets[i].getY();

                    // Check if the hornet will collide with the game boundaries or the hive
                    boolean willCollideWithBoundary =
                            hornetX + moveX <= gameArea.x || hornetX + moveX + hornets[i].getWidth() >= gameArea.x + gameArea.width ||
                                    hornetY + moveY <= gameArea.y || hornetY + moveY + hornets[i].getHeight() >= gameArea.y + gameArea.height;

                    Rectangle futureHornet = new Rectangle(hornetX + roundAwayFromZero(moveX), hornetY + roundAwayFromZero(moveY), hornets[i].getWidth(), hornets[i].getHeight());

                    boolean willCollideWithHive = futureHornet.intersects(hiveRectangle);

                    // If the hornet is about to collide with the hive or boundaries, reset direction
                    if (willCollideWithBoundary || willCollideWithHive) {
                        // Reset the direction to avoid getting stuck
                        hornets[i].framesRemaining = 0; // Force a direction change immediately
                    } else {
                        // If no collision is detected, move in the current direction
                        if (moveX < 0) {
                            hornets[i].moveRight(moveX);
                        } else if (moveX > 0) {
                            hornets[i].moveLeft(-moveX);
                        }
                        if (moveY < 0) {
                            hornets[i].moveUp(-moveY);
                        } else if (moveY > 0) {
                            hornets[i].moveDown(moveY);
                        }

                        // Decrease frames remaining in this direction
                        hornets[i].framesRemaining--;
                    }

                    // Special handling if the hornet is already inside the hive area
                    if (willCollideWithHive) {
                        // Force a new random direction to move away from the hive
                        hornets[i].directionX = -hornets[i].directionX;
                        hornets[i].directionY = -hornets[i].directionY;
                        hornets[i].framesRemaining = 60; // Reset frames for the new direction

                        // Try to move away from the hive by forcing a step in the new direction
                        double escapeMoveX = hornetSpeed * hornets[i].directionX;
                        double escapeMoveY = hornetSpeed * hornets[i].directionY;

                        // Ensure that the forced movement will not collide with boundaries
                        if (hornetX + escapeMoveX > gameArea.x && hornetX + escapeMoveX + hornets[i].getWidth() < gameArea.x + gameArea.width &&
                                hornetY + escapeMoveY > gameArea.y && hornetY + escapeMoveY + hornets[i].getHeight() < gameArea.y + gameArea.height) {

                            // Apply the escape movement
                            if (escapeMoveX < 0) {
                                hornets[i].moveLeft(-escapeMoveX);
                            } else if (escapeMoveX > 0) {
                                hornets[i].moveRight(escapeMoveX);
                            }
                            if (escapeMoveY < 0) {
                                hornets[i].moveUp(-escapeMoveY);
                            } else if (escapeMoveY > 0) {
                                hornets[i].moveDown(escapeMoveY);
                            }
                        }
                    }
                }
            }


        }
    }

    public static int roundAwayFromZero(double value) {
        if (value > 0) {
            return (int) Math.ceil(value);  // Round up for positive numbers
        } else {
            return (int) Math.floor(value);  // Round down for negative numbers
        }
    }

    private void checkFlowersCollisions() {
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

        // Get the current size of the panel
        int width = getWidth();
        int height = getHeight();

        // Calculate the scale factors
        double scaleX = (double) width / getPreferredSize().getWidth();
        double scaleY = (double) height / getPreferredSize().getHeight();

        // Use the smaller scale factor to maintain aspect ratio
        double scale = Math.min(scaleX, scaleY);

        // Create a transform to scale the graphics
        AffineTransform transform = new AffineTransform();
        transform.translate((width - getPreferredSize().getWidth() * scale) / 2, (height - getPreferredSize().getHeight() * scale) / 2);
        transform.scale(scale, scale);

        // Apply the transform to the Graphics2D object
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.transform(transform);

        // Clear the background
        g2d.setColor(new Color(89, 160, 74)); // Background color
        g2d.fillRect(0, 0, getPreferredSize().width, getPreferredSize().height);

        // Render game content
        for (Sprite sprite : grass) {
            sprite.render(g2d);
        }

        for (Sprite sprite : flowers) {
            sprite.render(g2d);
        }

        for (Sprite sprite : hornets) {
            sprite.render(g2d);
        }

        BufferedImage beehive = sheet.getSubimage(0, 41, 52, 58);
        g2d.drawImage(beehive, (int) ((getPreferredSize().getWidth() - beehive.getWidth()) / 2), (int) ((getPreferredSize().getHeight() - beehive.getHeight()) / 2), this);

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
        BufferedImage pollenIcon = sheet.getSubimage(63, 16, 17, 17);
        for (int i = 0; i < pollenCount; i++) {
            g2d.drawImage(pollenIcon, (int) (getPreferredSize().getWidth() + 10), 20 + 20 * i, this);
        }

        BufferedImage heart = sheet.getSubimage(81, 16, 15, 14);
        for (int i = 0; i < heartCount; i++) {
            g2d.drawImage(heart, -10 - heart.getWidth(), 20 + 20 * i, this);
        }

        // Draw points over the beehive
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Points: " + points, (int) (getPreferredSize().getWidth() / 2 - 20), (int) (getPreferredSize().getHeight() / 2 - 60));

        g2d.dispose();
    }


    @Override
    public void run() {
        while (running) {
            long lastLoopTime = System.nanoTime();
            updateGame();
            repaint();  // Render the game
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


