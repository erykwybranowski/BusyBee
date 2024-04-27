package lib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CustomWindow extends JFrame {
    private JLabel imageLabel;
    private JButton loadButton;
    private JButton textButton;
    private JButton rectangleButton;
    private JButton colorAreaButton;
    private JButton gradientAreaButton;
    private JPanel mainPanel;
    private BufferedImage img;

    public CustomWindow(String title, String loadButtonName) {
        super(title);
        initComponents(loadButtonName);
        setupUI();
        setupListeners();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);
    }

    private void initComponents(String loadButtonName) {
        mainPanel = new JPanel(new BorderLayout());
        imageLabel = new JLabel();
        imageLabel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                resizeImage();
            }
        });
        loadButton = new JButton(loadButtonName);
        textButton = new JButton("Tekst");
        rectangleButton = new JButton("Prostokąt");
        colorAreaButton = new JButton("Obszar kolor");
        gradientAreaButton = new JButton("Obszar gradient");
    }

    private void setupUI() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loadButton);
        buttonPanel.add(textButton);
        buttonPanel.add(rectangleButton);
        buttonPanel.add(colorAreaButton);
        buttonPanel.add(gradientAreaButton);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        getContentPane().add(mainPanel);
    }

    private void setupListeners() {
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(CustomWindow.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        img = ImageIO.read(selectedFile);
                        updateImage();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        textButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (img != null) {
                    drawText();
                    updateImage();
                }
            }
        });

        rectangleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (img != null) {
                    drawRectangle();
                    updateImage();
                }
            }
        });

        colorAreaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (img != null) {
                    fillColorArea(Color.RED);
                    updateImage();
                }
            }
        });

        gradientAreaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (img != null) {
                    fillGradientArea();
                    updateImage();
                }
            }
        });
    }

    private void drawText() {
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 100));
        g2d.drawString("Eryk Wybranowski", 100, 100);
        g2d.dispose();
    }

    private void drawRectangle() {
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(400, 400, 300, 200);
        g2d.dispose();
    }

    private void fillColorArea(Color color) {
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(100, 100, 200, 200);
        g2d.dispose();
    }

    private void fillGradientArea() {
        GradientPaint gradient = new GradientPaint(400, 100, Color.BLUE, 600, 300, Color.YELLOW);
        Graphics2D g2d = img.createGraphics();
        g2d.setPaint(gradient);
        g2d.fillRect(400, 100, 200, 200);
        g2d.dispose();
    }

    private void updateImage() {
        ImageIcon icon = new ImageIcon(img);
        imageLabel.setIcon(icon);
        resizeImage();
    }

    private void resizeImage() {
        if (imageLabel.getIcon() != null) {
            ImageIcon icon = (ImageIcon) imageLabel.getIcon();
            Image img = icon.getImage();
            int width = imageLabel.getWidth();
            int height = imageLabel.getHeight();
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImg));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new CustomWindow("Eryk Wybranowski", "Wczytaj");
            }
        });
    }
}
