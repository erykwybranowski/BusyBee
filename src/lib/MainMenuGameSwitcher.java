package lib;

import javax.swing.*;
import java.awt.*;

public class MainMenuGameSwitcher {
    private JFrame frame;
    private JPanel cards;
    private CardLayout cardLayout;
    private Game gamePanel;
    private MainMenu mainMenuPanel;

    public MainMenuGameSwitcher() {
        // Create the frame
        frame = new JFrame("Busy Bee");

        // Set frame properties
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true); // Remove title bar and borders

        // Create the panel and add it to the frame
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        frame.add(cards);

        // Create the main menu panel
        startNewMenu();

        // Configure full-screen mode
        setFullScreenMode();

        // Make the frame visible
        frame.setVisible(true);
    }

    private void setFullScreenMode() {
        // Get the GraphicsDevice associated with the default screen
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Check if full-screen mode is supported
        if (gd.isFullScreenSupported()) {
            // Apply full-screen mode
            gd.setFullScreenWindow(frame);
        } else {
            // If full-screen mode isn't supported, maximize the window
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setResizable(true);  // Allow resizing if not in full-screen mode
        }
    }

    // Method to start a fresh game
    public void startNewGame() {
        if (gamePanel != null) {
            cards.remove(gamePanel);  // Remove the old game panel
            gamePanel.stopGame();     // Stop the old game
        }
        gamePanel = new Game(cardLayout, cards, this);  // Create a fresh game instance
        cards.add(gamePanel, "game");
        cardLayout.show(cards, "game");
        gamePanel.startGame();  // Start the new game
        cards.revalidate();      // Ensure the layout is refreshed
        cards.repaint();
    }

    public void startNewMenu() {
        if (mainMenuPanel != null) {
            cards.remove(mainMenuPanel);  // Remove the old game panel
            mainMenuPanel.stopMenu();     // Stop the old game
        }
        mainMenuPanel = new MainMenu(cardLayout, cards, this);  // Create a fresh game instance
        cards.add(mainMenuPanel, "menu");
        cardLayout.show(cards, "menu");
        mainMenuPanel.start();  // Start the new game
        cards.revalidate();      // Ensure the layout is refreshed
        cards.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenuGameSwitcher());
    }
}
