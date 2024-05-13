package lib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenu extends JPanel {
    public MainMenu(CardLayout cardLayout, JPanel cards) {
        super(new BorderLayout());
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cards, "game");
            }
        });
        add(startButton, BorderLayout.CENTER);
    }
}
