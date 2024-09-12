package lib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JPanel {
    public MainMenu(CardLayout cardLayout, JPanel cards, MainMenuGameSwitcher switcher) {
        super(new BorderLayout());
        setPreferredSize(new Dimension(400, 400)); // Set preferred size to 400x400

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            switcher.startNewGame();  // Start a fresh game when clicking start
        });

        add(startButton, BorderLayout.CENTER);
    }
}


