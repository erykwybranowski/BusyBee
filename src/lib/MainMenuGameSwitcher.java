package lib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuGameSwitcher extends JFrame {
    public MainMenuGameSwitcher() {
        CardLayout cardLayout = new CardLayout();
        JPanel cards = new JPanel(cardLayout);
        setTitle("Nektar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the main menu panel
        JPanel mainMenuPanel = new MainMenu(cardLayout, cards);

        // Create the game panel
        JPanel gamePanel = new Game(cardLayout, cards);

        // Create a panel with CardLayout to switch between main menu and game
        cards.add(mainMenuPanel, "menu");
        cards.add(gamePanel, "game");

        add(cards);

        // Set the initial panel to display
        cardLayout.show(cards, "menu");
        pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
        int taskBarHeight = insets.bottom;
        setSize(screenSize.width, screenSize.height - taskBarHeight); // Set the frame size to screen size minus taskbar height
        setLocationRelativeTo(null); // Center the frame on the screen
        cards.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainMenuGameSwitcher().setVisible(true);
            }
        });
    }
}
