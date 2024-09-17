package lib;

import lib.animations.Sprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.Timer;

public class Game extends JPanel implements Runnable {
    private final CardLayout cardLayout;
    private final JPanel cards;
    private final MainMenuGameSwitcher switcher;
    Thread gameThread;
    private boolean running = false;
    private Random random;
    private final int TARGET_FPS = 60;  // Target frames per second
    private final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;  // Time per frame in nanoseconds
    private Set<String> pressedKeys = new HashSet<>();
    private String highscoreFilePath;
    private int highscore = 0;
    private Rectangle gameOverRect;
    private JButton returnToMenuButton;
    private Font font;
    private Set<PopUp> popUps = new HashSet<>();

    public BufferedImage sheet;
    private Sprite[] grass;
    private Sprite bee;
    private Sprite[] flowers;
    private Sprite[] hornets;

    private int hornetsCount = 5;
    private boolean[] hornetsMoved;
    private final double INITIAL_SPEED = 2;
    private static final double SPEED_REDUCTION = 0.4;
    private double currentSpeed = INITIAL_SPEED;
    private int pollenCount = 0;  // Number of times pollen is collected
    private int points = 0;  // Game points
    private static final int MAX_POLLEN = 3;
    private int heartCount = 3;
    private int grassCount = 300;
    private Timer grassTimer;
    private int flowerCount = 20;
    private double hornetSpeed = 0.8;
    private int framesToChangeHornetDirection = 120;

    private int healthBoostersCount = 2;
    private boolean[] healthBoostersMoved;
    private Sprite[] healthBoosters;
    private Timer healthBoostersTimer;

    private int comboBoostersCount = 2;
    private int[] comboBoostersMoved;
    private Sprite[] comboBoosters;
    private Timer comboBoostersTimer;
    private SoundManager soundManager = new SoundManager();

    public Game(CardLayout cardLayout, JPanel cards, MainMenuGameSwitcher switcher) {
        super(new BorderLayout());
        this.switcher = switcher;
        setPreferredSize(new Dimension(400, 400));
        setBackground(new Color(81, 121, 71));
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setUpKeyBindings();
        setFocusable(true);
        requestFocusInWindow();
        this.cardLayout = cardLayout;
        this.cards = cards;
        soundManager.playSound("/game_music.wav", true, 0.8f);
    }

    private void setUpGraphics() {
        // Load resources
        try {
            URL imageUrl = getClass().getResource("/sheet.png");
            sheet = ImageIO.read(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream is = getClass().getResourceAsStream("/GrapeSoda.ttf");
            if (is != null) {
                font = Font.createFont(Font.TRUETYPE_FONT, is);
            } else {
                System.out.println("Font not found!");
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            font = new Font("Monospaced", Font.PLAIN, 14); // Fallback font if loading fails
        }

        random = new Random();

        // Initialize sprites and bee
        Sprite[] tempGrass = new Sprite[grassCount];
        for (int i = 0; i < grassCount; i++) {
            tempGrass[i] = new Sprite(this, sheet, random.nextInt(10) + 1, 20 + random.nextInt(36) * 10, 30 + random.nextInt(21) * 16, 500);
        }
        grass = tempGrass;

        grassTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update current frame index
                for (Sprite sprite : grass) {
                    sprite.setCurrentFrameIndex((sprite.getCurrentFrameIndex() + 1) % sprite.getFramesLength());
                }
                repaint();
            }
        });
        grassTimer.start();

        bee = new Sprite(this, sheet, 11, ((int) getPreferredSize().getWidth() - 17) / 2, ((int) getPreferredSize().getHeight() - 15) / 2, 150);
        Rectangle hiveRectangle = new Rectangle(((int) getPreferredSize().getWidth() - 52) / 2, ((int) getPreferredSize().getHeight() - 58) / 2, 52, 58);

        Sprite[] tempHornets = new Sprite[hornetsCount];
        for (int i = 0; i < hornetsCount; i++) {
            tempHornets[i] = new Sprite(this, sheet, 17, -100, -100, 300);
        }
        hornets = tempHornets;
        hornetsMoved = new boolean[hornetsCount];

        Sprite[] tempHealthBoosters = new Sprite[healthBoostersCount];
        for (int i = 0; i < healthBoostersCount; i++) {
            tempHealthBoosters[i] = new Sprite(this, sheet, 18, -100, -100, -1);
        }
        healthBoosters = tempHealthBoosters;
        healthBoostersMoved = new boolean[healthBoostersCount];

        healthBoostersTimer = new Timer(new Random().nextInt(10000) + 10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < healthBoostersCount; i++) {
                    if (!healthBoostersMoved[i]) {
                        healthBoostersMoved[i] = true;
                        soundManager.playSound("/bonus_spawn.wav", false, 0.8f);
                        Sprite[][] list = {healthBoosters, comboBoosters};
                        healthBoosters[i].moveRandom(list);
                        healthBoostersTimer.setDelay(new Random().nextInt(10000) + 10000);
                        break;
                    }
                }
            }
        });
        healthBoostersTimer.start();

        Sprite[] tempComboBoosters = new Sprite[comboBoostersCount];
        for (int i = 0; i < comboBoostersCount; i++) {
            tempComboBoosters[i] = new Sprite(this, sheet, 19, -100, -100, -1);
        }
        comboBoosters = tempComboBoosters;
        comboBoostersMoved = new int[comboBoostersCount];
        for (int i = 0; i < comboBoostersCount; i++) {
            comboBoostersMoved[i] = 0;
        }

        comboBoostersTimer = new Timer(new Random().nextInt(10000) + 10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < comboBoostersCount; i++) {
                    if (comboBoostersMoved[i] == 0) {
                        comboBoostersMoved[i] = 1;
                        soundManager.playSound("/bonus_spawn.wav", false, 0.8f);
                        Sprite[][] list = {healthBoosters, comboBoosters};
                        comboBoosters[i].moveRandom(list);
                        comboBoostersTimer.setDelay(new Random().nextInt(10000) + 10000);
                        break;
                    }
                }
            }
        });
        comboBoostersTimer.start();

        flowers = spreadSprites(12, 5, hiveRectangle, flowerCount);

        setUpGameOverScreen();
    }

    private Sprite[] spreadSprites(int typeRangeFirst, int typeRangeCount, Rectangle hiveRectangle, int spriteCount) {
        Sprite[] tempSprites = new Sprite[spriteCount];

        for (int i = 0; i < spriteCount; i++) {
            boolean valid = false;
            do {
                Sprite newSprite = new Sprite(this, sheet, random.nextInt(typeRangeCount) + typeRangeFirst, 20 + random.nextInt(9) * 40, 30 + random.nextInt(5) * 67, 0);
                Rectangle newSpriteBounds = newSprite.getBounds();

                // Check if the new flower intersects with the rectangle
                boolean intersectsRectangle = newSpriteBounds.intersects(hiveRectangle);

                // Check if the new flower intersects with any of the existing flowers
                boolean intersectsOtherSprites = false;
                for (int j = 0; j < i; j++) {
                    if (newSpriteBounds.intersects(tempSprites[j].getBounds())) {
                        intersectsOtherSprites = true;
                        break;
                    }
                }
                if (!intersectsRectangle && !intersectsOtherSprites) {
                    valid = true;
                    tempSprites[i] = newSprite;
                }
            } while (!valid);
        }
        return tempSprites;
    }

    private void setUpButtons(CardLayout cardLayout, JPanel cards) {
        ImageIcon backButtonIcon = new ImageIcon(sheet.getSubimage(34, 137, 16, 13));  // Coordinates of the "Back" icon
        // Scale the icon
        Image scaledBackButtonImage = backButtonIcon.getImage().getScaledInstance(96, 78, Image.SCALE_DEFAULT);
        ImageIcon scaledBackButtonIcon = new ImageIcon(scaledBackButtonImage);

        // Create the button with the scaled icon
        JButton backButton = new JButton(scaledBackButtonIcon);
        backButton.setBorderPainted(false);  // Remove button borders
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);  // Make button background transparent

        // Set position manuall
        setLayout(null);  // Disable layout manager
        backButton.setBounds(0, 20, 100, 100);  // Position it similarly to the hearts

        // Add action listener for the button
        backButton.addActionListener(e -> returnToMenu());

        add(backButton);
    }

    private void setUpGameOverScreen() {
        // Initialize the game over rectangle
        gameOverRect = new Rectangle(100, 140, 200, 120);

        // Create "Return to Menu" button with icon
        int buttonHeight = 70;
        int buttonWidth = 230;
        ImageIcon returnToMenuIcon = new ImageIcon(sheet.getSubimage(34, 129, 23, 7)); // Coordinates for "Return to Menu" icon
        Image scaledReturnToMenuImage = returnToMenuIcon.getImage().getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_DEFAULT);
        ImageIcon scaledReturnToMenuIcon = new ImageIcon(scaledReturnToMenuImage);

        returnToMenuButton = new JButton("Wróć do Menu", scaledReturnToMenuIcon);

        // Set font and text color for the button text
        returnToMenuButton.setFont(font.deriveFont(Font.BOLD, 20f)); // Use the custom font
        returnToMenuButton.setForeground(Color.WHITE); // Set text color to white

        // Remove borders and background
        returnToMenuButton.setBorderPainted(false);
        returnToMenuButton.setFocusPainted(false);
        returnToMenuButton.setContentAreaFilled(false); // Make background transparent

        // Align the text in the center of the button, on top of the icon
        returnToMenuButton.setHorizontalTextPosition(JButton.CENTER);
        returnToMenuButton.setVerticalTextPosition(JButton.CENTER);

        // Calculate button position relative to the game area
        int gameAreaWidth = 400;
        int gameAreaX = (int) ((getWidth() - gameAreaWidth) / 2); // X coordinate of game area relative to the screen
        int gameAreaY = (int) ((getHeight() - getPreferredSize().getHeight()) / 2); // Y coordinate of game area relative to the screen

        // Set button position and size relative to the game area
        returnToMenuButton.setBounds(gameAreaX + (gameAreaWidth - buttonWidth) / 2,
                gameAreaY + (int) ((gameAreaWidth - buttonHeight) / 1.5), buttonWidth, buttonHeight); // Center button below high score

        // Add action listener for returning to menu
        returnToMenuButton.addActionListener(e -> returnToMenu());

        // Add the button to the panel
        setLayout(null); // Use absolute positioning
        add(returnToMenuButton);

        // Initially hide the button
        returnToMenuButton.setVisible(false);
    }

    private void returnToMenu() {
        soundManager.playSound("/button.wav", false, 1);
        stopGame();  // Stop the game when going back to the main menu
        this.switcher.startNewMenu();
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
            setUpGraphics();
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
            setUpButtons(cardLayout, cards);
        }
    }

    public void stopGame() {
        running = false;

        pressedKeys.clear();
        bee.stopAnimation();
        Sprite[][] sprites = {grass, flowers, hornets, comboBoosters, healthBoosters};
        for (Sprite[] spriteArray : sprites) {
            for (Sprite sprite : spriteArray) {
                sprite.stopAnimation();
            }
        }
        healthBoostersTimer.stop();
        comboBoostersTimer.stop();
        grassTimer.stop();
        soundManager.stopAllSounds();
        try {
            if (gameThread != null && gameThread.isAlive()) {
                gameThread.join();  // Wait for the game thread to fully stop
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gameThread = null;  // Reset the thread to null so it can be restarted
    }

    private void showGameOverScreen() {
        loadHighscore();  // Load the current highscore at game over

        if (points > highscore) {
            highscore = points;
            saveHighscore(highscore);  // Save the new highscore
        }
        soundManager.playSound("/game_over.wav", false, 1);
        soundManager.stopSound("/buzz.wav");
        running = false;
        returnToMenuButton.setVisible(true);
        repaint(); // Trigger a repaint to show the game over screen
    }

    private void updateGame() {
        Rectangle gameArea = new Rectangle(20, 20, 360, 350);
        Rectangle beeBounds = bee.getBounds();
        boolean movesHorizontally = false;
        boolean movesVertically = false;
        if (pressedKeys.contains("LEFT") || pressedKeys.contains("RIGHT")) {
            movesHorizontally = true;
        }
        if (pressedKeys.contains("UP") || pressedKeys.contains("DOWN")) {
            movesVertically = true;
        }
        double localSpeed = currentSpeed;
        if (movesVertically && movesHorizontally) {
            localSpeed = currentSpeed / Math.sqrt(2);
        }

        if (pressedKeys.contains("LEFT") && beeBounds.x > gameArea.x) {
            bee.moveLeft(localSpeed);
        }

        if (pressedKeys.contains("RIGHT") && beeBounds.x + beeBounds.width < gameArea.x + gameArea.width) {
            bee.moveRight(localSpeed);
        }

        if (pressedKeys.contains("UP") && beeBounds.y > gameArea.y) {
            bee.moveUp(localSpeed);
        }

        if (pressedKeys.contains("DOWN") && beeBounds.y + beeBounds.height < gameArea.y + gameArea.height) {
            bee.moveDown(localSpeed);
        }
        Set<String> fullSet = new HashSet<>(Arrays.asList("LEFT", "RIGHT", "UP", "DOWN"));
        if (!Collections.disjoint(pressedKeys, fullSet)) {
            if (!soundManager.isPlaying("/buzz.wav")) {
                soundManager.playSound("/buzz.wav", true, 1);  // Loop the movement sound
            }
        } else {
            soundManager.stopSound("/buzz.wav");
        }

        defineHornetsMovement(gameArea);
        // Check for collisions with flowers
        checkFlowersCollisions();
        checkHornetsCollisions();

        defineHealthBoosters();
        defineComboBoosters();

        // Handle interaction with beehive
        BufferedImage beehiveImage = sheet.getSubimage(0, 41, 52, 58);
        Rectangle beehiveBounds = new Rectangle(((int) getPreferredSize().getWidth() - beehiveImage.getWidth()) / 2,
                ((int) getPreferredSize().getHeight() - beehiveImage.getHeight()) / 2,
                beehiveImage.getWidth(), beehiveImage.getHeight());

        if (bee.getBounds().intersects(beehiveBounds)) {
            int combo = countCombo();
            int pointsAdd = pollenCount * 10 * combo;
            points += pointsAdd; // Add points based on how much pollen was collected
            if (pointsAdd > 0) {
                soundManager.playSound("/hive_points.wav", false, 1);
                popUps.add(new PopUp("+" + pointsAdd, bee.getX(), bee.getY(), ((double) (combo - 1) / 11 + 1)));
            } else if (combo > 1) {
                soundManager.playSound("/hive_reset.wav", false, 1);
            }
            for (int i = 0; i < comboBoostersCount; i++) {
                if (comboBoostersMoved[i] == 2) {
                    comboBoostersMoved[i] = 0;
                }
            }
            pollenCount = 0;        // Reset pollen count
            currentSpeed = INITIAL_SPEED;  // Reset speed
        }
    }

    private void checkHornetsCollisions() {
        for (int i = 0; i < hornetsCount; i++) {
            if (hornets[i].getBounds().intersects(bee.getBounds())) {
                pollenCount = 0;
                heartCount--;
                soundManager.playSound("/hit.wav", false, 1);
                bee.setX((int) (getPreferredSize().getWidth() / 2) - bee.getWidth() / 2);
                bee.setY((int) (getPreferredSize().getHeight() / 2) - bee.getHeight() / 2);
                if (heartCount == 0) {
                    showGameOverScreen();
                }
            }
        }
    }

    private void defineHornetsMovement(Rectangle gameArea) {
        Rectangle hiveRectangle = new Rectangle(((int) getPreferredSize().getWidth() - 52) / 2, ((int) getPreferredSize().getHeight() - 58) / 2, 52, 58);

        for (int i = 0; i < hornetsCount; i++) {
            if (!hornetsMoved[i]) {
                if (points >= 100 && i == 0 || points >= i * 500 && i > 0) {
                    Sprite[][] list = {hornets};
                    hornets[i].moveRandom(list);
                    hornetsMoved[i] = true;
                    soundManager.playSound("/hornet_spawn.wav", false, 1);
                }
            } else if (!bee.getBounds().intersects(hiveRectangle.getBounds())) {
                // Get bee and hornet positions
                int beeX = bee.getX();
                int beeY = bee.getY();
                int beeWidth = bee.getWidth();
                int beeHeight = bee.getHeight();
                int hornetX = hornets[i].getX();
                int hornetY = hornets[i].getY();
                int hornetWidth = hornets[i].getWidth();
                int hornetHeight = hornets[i].getHeight();
                int hiveX = hiveRectangle.x;
                int hiveY = hiveRectangle.y;
                int hiveWidth = hiveRectangle.width;
                int hiveHeight = hiveRectangle.height;

// Calculate direction vector from hornet to bee
                int deltaX = beeX - hornetX;
                int deltaY = beeY - hornetY;

// Calculate the distance between the hornet and the bee
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

// Normalize the direction vector and multiply by hornetSpeed
                if (distance != 0) {
                    double moveX = (hornetSpeed * (deltaX / distance));
                    double moveY = (hornetSpeed * (deltaY / distance));

                    // Predict the hornet's position after movement in x and y directions
                    Rectangle hornetMovedX = new Rectangle(hornetX + roundAwayFromZero(moveX), hornetY, hornetWidth, hornetHeight);
                    Rectangle hornetMovedY = new Rectangle(hornetX, hornetY + roundAwayFromZero(moveY), hornetWidth, hornetHeight);
                    Rectangle hornetMovedXY = new Rectangle(hornetX + roundAwayFromZero(moveX), hornetY + roundAwayFromZero(moveY), hornetWidth, hornetHeight);
                    boolean willIntersectX = hornetMovedX.intersects(hiveRectangle);
                    boolean willIntersectY = hornetMovedY.intersects(hiveRectangle);
                    if (willIntersectY) {
                        //if the bee is on the left or on the right of the hive
                        if (beeX + beeWidth <= hiveX || beeX >= hiveX + hiveWidth) {
                            if (moveX == 0){
                                if (hornetX < hiveX) {
                                    moveX = -1;
                                }
                                if (hornetX + hornetWidth > hiveX + hiveWidth) {
                                    moveX = 1;
                                }
                            }
                            hornets[i].moveHorizontally(moveX, hornetSpeed);
                        }
                        //if the bee is below or above the hive, move depending on the shorter route left or right
                        else if(hornetX - hiveX < hiveX + hiveWidth - (hornetX + hornetWidth)) {
                            hornets[i].moveRight(-hornetSpeed); // move Left
                        } else {
                            hornets[i].moveLeft(-hornetSpeed); // move Right
                        }
                    //if the hornet will hit the hive from left or right
                    } else if (willIntersectX) {
                        //if the bee is over or below the hive
                        if (beeY + beeHeight <= hiveY || beeY >= hiveY + hiveHeight) {
                            if (moveY == 0){
                                if (hornetY < hiveY) {
                                    moveY = -1;
                                }
                                if (hornetY + hornetHeight > hiveY + hiveHeight) {
                                    moveY = 1;
                                }
                            }
                            hornets[i].moveVertically(moveY, hornetSpeed);
                        }
                        //if the bee is on the left or on the right of the hive, move depending on the shorter route up or down
                        else if(hornetY - hiveY < hiveY + hiveHeight - (hornetY + hornetHeight)) {
                            hornets[i].moveUp(hornetSpeed); //move Up
                        } else {
                            hornets[i].moveDown(hornetSpeed); // move Down
                        }
                    //normal condition, no hive in reach
                    } else {
                        if (moveX < 0) {
                            hornets[i].moveRight(moveX);
                        }
                        if (moveX > 0) {
                            hornets[i].moveLeft(-moveX);
                        }
                        if (moveY < 0) {
                            hornets[i].moveUp(-moveY);
                        }
                        if (moveY > 0) {
                            hornets[i].moveDown(moveY);
                        }
                    }
                }
            } else {
                if (hornets[i].framesRemaining <= 0) {
                    // Choose a new random direction
                    double angle = Math.random() * 2 * Math.PI; // Random angle in radians
                    hornets[i].directionX = Math.cos(angle);
                    hornets[i].directionY = Math.sin(angle);
                    hornets[i].framesRemaining = framesToChangeHornetDirection; // Move in this direction for 60 frames
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
                soundManager.playSound("/pollen_collect.wav", false, 1);
                int comboBefore = countCombo();
                pollenCount++;
                currentSpeed = Math.max(0, currentSpeed - SPEED_REDUCTION); // Reduce speed but ensure it's not negative
                flower.collectPollen(this);
                int comboAfter = countCombo();
                if (comboAfter > 1 && comboAfter != comboBefore) {
                    popUps.add(new PopUp("Combo x" + comboAfter, bee.getX(), bee.getY(), ((double) (comboAfter - 1) / 11 + 1)));
                }
            }
        }
    }

    private void defineHealthBoosters() {
        for (int i = 0; i < healthBoostersCount; i++) {
            if (bee.getBounds().intersects(healthBoosters[i].getBounds())) {
                soundManager.playSound("/bonus_collect.wav", false, 1);
                healthBoosters[i].setX(-100);
                healthBoosters[i].setY(-100);
                healthBoostersMoved[i] = false;
                if (heartCount < 3) {
                    heartCount++;
                }
            }
        }
    }

    private void defineComboBoosters() {
        for (int i = 0; i < comboBoostersCount; i++) {
            if (bee.getBounds().intersects(comboBoosters[i].getBounds())) {
                soundManager.playSound("/bonus_collect.wav", false, 1);
                comboBoosters[i].setX(-100);
                comboBoosters[i].setY(-100);
                comboBoostersMoved[i] = 2;
                int combo = countCombo();
                popUps.add(new PopUp("Combo x" + combo, bee.getX(), bee.getY(), ((double) (combo - 1) / 11 + 1)));
            }
        }
    }

    private void setHighscoreFilePath() {
        String userHome = System.getProperty("user.home");
        String folderPath = userHome + File.separator + "AppData" + File.separator + "Local" + File.separator + "BusyBee";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();  // Create directories if they don't exist
        }
        highscoreFilePath = folderPath + File.separator + "highscore2.txt";
    }

    // Load the highscore from the file
    private void loadHighscore() {
        setHighscoreFilePath();
        File highscoreFile = new File(highscoreFilePath);

        if (highscoreFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(highscoreFile))) {
                String line = reader.readLine();
                if (line != null && line.startsWith("Highscore=")) {
                    highscore = Integer.parseInt(line.split("=")[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Save the new highscore to the file
    private void saveHighscore(int newHighscore) {
        setHighscoreFilePath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(highscoreFilePath))) {
            writer.write("Highscore=" + newHighscore);
        } catch (IOException e) {
            e.printStackTrace();
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

        for (Sprite sprite : healthBoosters) {
            sprite.render(g2d);
        }

        for (Sprite sprite : comboBoosters) {
            sprite.render(g2d);
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

        FontMetrics metrics;
        // Draw points
        g2d.setFont(font.deriveFont(20f));
        metrics = g2d.getFontMetrics(); // Get FontMetrics for the current font
        String pointsText = "" + points + " punktów";
        int pointsTextWidth = metrics.stringWidth(pointsText);
        int pointsTextHeight = metrics.getHeight(); // Get the height of the text

        // Calculate position for centering the text
        int pointsTextX = (int) (getPreferredSize().getWidth() / 2 - pointsTextWidth / 2);
        int pointsTextY = 15;  // The Y position where the text will be drawn

        // Set background color and draw the rectangle
        g2d.setColor(new Color(78, 47, 19));
        g2d.fillRect(pointsTextX - 7, pointsTextY - pointsTextHeight + 7, pointsTextWidth + 14, pointsTextHeight + 4);
        // Set background color and draw the rectangle
        g2d.setColor(new Color(142, 106, 77));
        g2d.fillRect(pointsTextX - 5, pointsTextY - pointsTextHeight + 9, pointsTextWidth + 10, pointsTextHeight);

        // Set the text color and draw the points text over the rectangle
        g2d.setColor(new Color(255, 255, 255));
        g2d.drawString(pointsText, pointsTextX, pointsTextY + 2);

        int combo = countCombo();
        if (combo > 1) {
            g2d.setColor(new Color(255, 231, 78));
        }
        String comboText = "x" + combo;
        g2d.drawString(comboText, (int) (getPreferredSize().getWidth() + 10), 90);

        Iterator<PopUp> iterator = popUps.iterator();
        while (iterator.hasNext()) {
            PopUp popUp = iterator.next();
            float size = (float) (20 * popUp.getSizeMultiplier());
            g2d.setFont(font.deriveFont(size));
            metrics = g2d.getFontMetrics();
            int popUpTextWidth = metrics.stringWidth(popUp.getText());
            int popUpTextX = (popUp.getX() + (bee.getWidth() - popUpTextWidth) / 2);
            int popUpTextY = (popUp.getY() - 10);

            int alpha = (int) ((popUp.getFrames() / 60.0) * 255);
            alpha = Math.max(0, Math.min(alpha, 255)); // Ensure alpha is in the range 0-255

            g2d.setColor(new Color(255, 231, 78, alpha));
            g2d.drawString(popUp.getText(), popUpTextX, popUpTextY - (60 - popUp.getFrames()));
            popUp.reduceFrames();

            if (popUp.getFrames() <= 0) {
                iterator.remove();
            }
        }

        if (!running) {
            // Draw the Game Over window
            g2d.drawImage(sheet.getSubimage(0, 129, 33, 20), gameOverRect.x, gameOverRect.y, gameOverRect.width, gameOverRect.height, this);

            // Center "Game over" text horizontally
            g2d.setFont(font.deriveFont(24f));
            metrics = g2d.getFontMetrics(); // Get FontMetrics for the new font size
            String gameOverText = "Koniec gry";
            int gameOverTextWidth = metrics.stringWidth(gameOverText);

            g2d.setColor(Color.WHITE);
            g2d.drawString(gameOverText, (int) (gameOverRect.x + (gameOverRect.width / 2 - gameOverTextWidth / 2)), gameOverRect.y + 30);

            // Check if it's a new highscore or not
            String highscoreText;
            g2d.setFont(font.deriveFont(20f));
            metrics = g2d.getFontMetrics(); // Get FontMetrics for the new font size
            if (points == highscore) {
                g2d.setColor(new Color(255, 231, 78));
                highscoreText = "Nowy rekord!";
            } else {
                g2d.setColor(new Color(186, 186, 186));
                highscoreText = "Rekord: " + highscore;
            }

            // Draw highscore text under the game over message
            int highscoreTextWidth = metrics.stringWidth(highscoreText);
            g2d.drawString(highscoreText, (int) (gameOverRect.x + (gameOverRect.width / 2 - highscoreTextWidth / 2)), gameOverRect.y + 55);
        }

        g2d.dispose();
    }

    private int countCombo() {
        int comboFromBoosters = 1;
        for (int value : comboBoostersMoved) {
            if (value == 2) {
                comboFromBoosters *= 2;
            }
        }
        int comboFromPollen = 1;
        if (pollenCount > 0) {
            comboFromPollen = pollenCount;
        }
        return comboFromPollen * comboFromBoosters;
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


