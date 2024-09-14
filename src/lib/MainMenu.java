package lib;

import lib.animations.Sprite;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

public class MainMenu extends JPanel implements Runnable {

    private boolean running = true; // To control the animation loop
    private int beeX, beeY; // Coordinates for the bee
    private int[] hornetX, hornetY; // Coordinates for the hornets
    private boolean movingRight = true; // To track the bee's direction
    private static final int OPTIMAL_TIME = 1000000000 / 60; // 60 FPS

    private CardLayout cardLayout;
    private JPanel cards;
    private Sprite bee;
    private Sprite[] hornets;
    private Sprite[] grass;
    private Sprite[] flowers;
    private Rectangle beehiveRect;
    private int beeSpeed = 2;
    private boolean beeGoingRight = true;
    private Font font;
    private JButton newGameButton;
    private JButton exitGameButton;
    public BufferedImage sheet;
    private MainMenuGameSwitcher switcher;
    private Timer grassTimer;
    private Thread menuThread;

    public MainMenu(CardLayout cardLayout, JPanel cards, MainMenuGameSwitcher switcher) {
        this.switcher = switcher;
        this.cardLayout = cardLayout;
        this.cards = cards;

        // Set up background color
        setBackground(new Color(89, 160, 74));
        setPreferredSize(new Dimension(600, 400));  // Adjust size as needed
        setLayout(null);  // Absolute positioning for buttons
        setUpGraphics();
    }

    private void setUpGraphics() {
        try {
            URL imageUrl = getClass().getResource("/sheet.png");
            sheet = ImageIO.read(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Load font
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

        // Set up sprites for grass, flowers, bee, hornets
        Random random = new Random();

        // Grass sprites
        int grassCount = 500; // Adjust the number as needed
        grass = new Sprite[grassCount];
        for (int i = 0; i < grassCount; i++) {
            grass[i] = new Sprite(this, sheet, random.nextInt(10) + 1, random.nextInt(59) * 10, random.nextInt(39) * 10, 500);
        }

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

        // Flower sprites
        int flowerCount = 40;  // Adjust the number as needed
        flowers = new Sprite[flowerCount];
        for (int i = 0; i < flowerCount; i++) {
            flowers[i] = new Sprite(this, sheet, random.nextInt(5) + 12, random.nextInt(10) * 65, 30+ random.nextInt(6) * 67, 0);
        }

        // Bee sprite
        bee = new Sprite(this,sheet, 11, -50, 360, 150);  // Starting just outside the left edge

        // Hornets sprites
        int hornetCount = 5;
        hornets = new Sprite[hornetCount];
        for (int i = 0; i < hornetCount; i++) {
            hornets[i] = new Sprite(this, sheet,17, -100 - i * 50, 360, 300);  // Following the bee
        }

        // Beehive rectangle (static)
        beehiveRect = new Rectangle(50, 200, 52, 58);  // Adjust position and size as needed
    }

    private void setUpTitleAndButtons() {
        // Get current panel width and height dynamically for proper scaling and centering
        int gameAreaWidth = getWidth();
        int gameAreaHeight = getHeight();

        // Button dimensions as a percentage of the available space
        int buttonHeight = (int) (gameAreaHeight * 0.12);  // 12% of height
        int buttonWidth = (int) (gameAreaWidth * 0.5);     // 50% of width

        // Create the title label "Busy Bee"
        JLabel titleLabel = new JLabel("Busy Bee");
        titleLabel.setFont(font.deriveFont(Font.BOLD, (float) (gameAreaHeight * 0.2)));  // Title font size is 20% of height
        titleLabel.setForeground(Color.BLACK);

        // Center the title relative to the full width of the window
        int titleX = (gameAreaWidth - titleLabel.getPreferredSize().width) / 2;
        int titleY = (int) (gameAreaHeight * 0.05);  // 5% from the top
        titleLabel.setBounds(titleX, titleY, titleLabel.getPreferredSize().width, titleLabel.getPreferredSize().height);
        add(titleLabel);

        // Create two shadow labels for the title (to give a 3D effect)
        JLabel titleLabel2 = new JLabel("Busy Bee");
        titleLabel2.setFont(font.deriveFont(Font.BOLD, (float) (gameAreaHeight * 0.2)));  // Title font size is 10% of height
        titleLabel.setForeground(new Color(255, 231, 78));

        // Center the title relative to the full width of the window
        int titleX2 = (gameAreaWidth - titleLabel2.getPreferredSize().width) / 2;
        int titleY2 = (int) (gameAreaHeight * 0.05);  // 5% from the top
        titleLabel2.setBounds(titleX2 + 2, titleY2 + 2, titleLabel2.getPreferredSize().width, titleLabel2.getPreferredSize().height);
        add(titleLabel2);

        // Create the title label "Busy Bee"
        JLabel titleLabel3 = new JLabel("Busy Bee");
        titleLabel3.setFont(font.deriveFont(Font.BOLD, (float) (gameAreaHeight * 0.2)));  // Title font size is 10% of height
        titleLabel3.setForeground(Color.WHITE);

        // Center the title relative to the full width of the window
        int titleX3 = (gameAreaWidth - titleLabel3.getPreferredSize().width) / 2;
        int titleY3 = (int) (gameAreaHeight * 0.05);  // 5% from the top
        titleLabel3.setBounds(titleX3 - 2, titleY3 - 2, titleLabel3.getPreferredSize().width, titleLabel3.getPreferredSize().height);
        add(titleLabel3);

        // Initialize "New Game" button
        ImageIcon newGameIcon = new ImageIcon(sheet.getSubimage(34, 129, 23, 7));  // Sprite for button icon
        Image scaledNewGameImage = newGameIcon.getImage().getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_DEFAULT);
        ImageIcon scaledNewGameIcon = new ImageIcon(scaledNewGameImage);

        newGameButton = new JButton("Nowa Gra", scaledNewGameIcon);  // Initialize button
        newGameButton.setFont(font.deriveFont(Font.BOLD, (float) (gameAreaHeight * 0.05)));  // Font size 5% of height
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setBorderPainted(false);
        newGameButton.setFocusPainted(false);
        newGameButton.setContentAreaFilled(false);
        newGameButton.setHorizontalTextPosition(JButton.CENTER);
        newGameButton.setVerticalTextPosition(JButton.CENTER);

        // Position the "New Game" button
        int newGameButtonX = (gameAreaWidth - buttonWidth) / 2;
        int newGameButtonY = (int) (gameAreaHeight * 0.3);  // 30% from the top
        newGameButton.setBounds(newGameButtonX, newGameButtonY, buttonWidth, buttonHeight);
        newGameButton.addActionListener(e -> startNewGame());
        add(newGameButton);

        // Initialize "Exit Game" button
        ImageIcon exitGameIcon = new ImageIcon(sheet.getSubimage(34, 129, 23, 7));
        Image scaledExitGameImage = exitGameIcon.getImage().getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_DEFAULT);
        ImageIcon scaledExitGameIcon = new ImageIcon(scaledExitGameImage);

        exitGameButton = new JButton("Wyjdź z Gry", scaledExitGameIcon);
        exitGameButton.setFont(font.deriveFont(Font.BOLD, (float) (gameAreaHeight * 0.05)));
        exitGameButton.setForeground(Color.WHITE);
        exitGameButton.setBorderPainted(false);
        exitGameButton.setFocusPainted(false);
        exitGameButton.setContentAreaFilled(false);
        exitGameButton.setHorizontalTextPosition(JButton.CENTER);
        exitGameButton.setVerticalTextPosition(JButton.CENTER);

        // Position the "Exit Game" button
        int exitGameButtonX = (gameAreaWidth - buttonWidth) / 2;
        int exitGameButtonY = (int) (gameAreaHeight * 0.45);  // 45% from the top
        exitGameButton.setBounds(exitGameButtonX, exitGameButtonY, buttonWidth, buttonHeight);
        exitGameButton.addActionListener(e -> System.exit(0));
        add(exitGameButton);

        // Add a rectangle with texture and rules under "Exit Game" button
        int rulesBoxY = exitGameButtonY + buttonHeight + (int) (gameAreaHeight * 0.04);  // Position below Exit button

        ImageIcon rulesIcon = new ImageIcon(sheet.getSubimage(0, 129, 33, 20));  // Extract texture for the rectangle
        JLabel rulesLabel = new JLabel(rulesIcon);  // Use the texture as a background

// Define the size of the rules box relative to the window width/height
        int rulesBoxWidth = buttonWidth;  // Same width as the button
        int rulesBoxHeight = (int) (gameAreaHeight * 0.25);  // 20% of the window height

// Scale the texture to fit the rules box dimensions
        Image rulesImage = rulesIcon.getImage().getScaledInstance(rulesBoxWidth, rulesBoxHeight, Image.SCALE_DEFAULT);
        ImageIcon scaledRulesIcon = new ImageIcon(rulesImage);
        rulesLabel.setIcon(scaledRulesIcon);

// Set bounds for the rules box and position it below the "Exit Game" button
        int rulesBoxX = (gameAreaWidth - rulesBoxWidth) / 2;
        rulesLabel.setBounds(rulesBoxX, rulesBoxY, rulesBoxWidth, rulesBoxHeight);

// Create a JPanel to hold the rules text (transparent panel)
        JPanel rulesPanel = new JPanel();
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));  // Vertical layout for text lines
        rulesPanel.setOpaque(false);  // Make the panel background transparent

// Define the rules as separate JLabels
        String[] rulesText = {
                "Steruj strzałkami/WASD.",
                "Zbieraj pyłek do ula i łap bonusy!",
                "Im więcej pyłku nosisz, tym wolniej lecisz...",
                "...ale zwiększasz też combo!",
                "Uciekaj przed szerszeniami.",
                "Ul jest od nich bezpieczny!"
        };

// Add each line of the rules as a JLabel and set the custom font
        for (String line : rulesText) {
            JLabel ruleLabel = new JLabel(line);
            ruleLabel.setFont(font.deriveFont(Font.PLAIN, (float) (gameAreaHeight * 0.03)));  // Set custom font
            ruleLabel.setForeground(Color.WHITE);  // Set text color
            ruleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);  // Center text horizontally
            rulesPanel.add(ruleLabel);  // Add each label to the panel
        }

// Position the rules panel inside the textured rules box
        rulesPanel.setBounds(0, 0, rulesBoxWidth, rulesBoxHeight);
        rulesPanel.setBorder(BorderFactory.createEmptyBorder((int) (rulesBoxHeight * 0.1), 0, 0, 0));  // 15% padding at the top

// Add the rules panel inside the rulesLabel (texture)
        rulesLabel.add(rulesPanel);
        add(rulesLabel);  // Add the rules label to the main panel
        this.cards.revalidate();
        this.cards.repaint();
    }

    private void startNewGame() {
        stopMenu();
        switcher.startNewGame();
    }

    public void stopMenu() {
        running = false;
        bee.stopAnimation();
        Sprite[][] sprites = {grass, flowers, hornets};
        for (Sprite[] spriteArray : sprites) {
            for (Sprite sprite : spriteArray) {
                sprite.stopAnimation();
            }
        }

        try {
            if (menuThread != null && menuThread.isAlive()) {
                menuThread.join();  // Wait for the menu thread to fully stop
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        menuThread = null;  // Reset the thread to null so it can be restarted
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

        // Draw grass and flowers
        for (Sprite sprite : grass) {
            sprite.render(g2d);
        }

        // Draw beehive on the left
        g2d.drawImage(sheet.getSubimage(0, 41, 52, 58), beehiveRect.x, beehiveRect.y, beehiveRect.width, beehiveRect.height, this);

        for (Sprite sprite : flowers) {
            sprite.render(g2d);
        }

        // Draw bee and hornets
        bee.render(g2d);
        for (Sprite hornet : hornets) {
            hornet.render(g2d);
        }
    }

    // The animation thread's run method
    @Override
    public void run() {
        while (running) {
            long lastLoopTime = System.nanoTime();

            animateBeeAndHornets();

            // Synchronize FPS
            long now = System.nanoTime();
            long updateLength = now - lastLoopTime;
            lastLoopTime = now;
            long sleepTime = (OPTIMAL_TIME - updateLength) / 1000000;

            try {
                Thread.sleep(Math.max(0, sleepTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // To start the animation thread
    public void start() {
        if (menuThread == null || !menuThread.isAlive()) {
            // Load resources and sprites
            running = true;
            menuThread = new Thread(this);
            menuThread.start();
            SwingUtilities.invokeLater(() -> setUpTitleAndButtons());
        }
    }

    private void animateBeeAndHornets() {
        // Animate bee
        if (beeGoingRight) {

            bee.moveRight(beeSpeed);  // Use moveRight method
            if (bee.getX() > getPreferredSize().getWidth() + 300) {  // If it flies off the screen
                beeGoingRight = false;
                for (int i = 0; i < hornets.length; i++) {
                    hornets[i].setX(bee.getX() + 50 * (i+1));
                }
            }
        } else {
            bee.moveLeft(beeSpeed);  // Use moveLeft method
            if (bee.getX() < - 300) {  // If it flies off the left screen
                beeGoingRight = true;
                for (int i = 0; i < hornets.length; i++) {
                    hornets[i].setX(bee.getX() - 50 * (i+1));
                }
            }
        }

        // Animate hornets following the bee
        for (int i = 0; i < hornets.length; i++) {
            if (beeGoingRight) {
                hornets[i].moveLeft(-beeSpeed);  // Gradual speed difference for hornets
            } else {
                hornets[i].moveRight(-beeSpeed);  // Same here, slower trailing
            }
        }

        repaint();  // Redraw the menu with updated positions
    }

}
