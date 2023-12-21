import javax.swing.*;
import java.awt.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Image Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            ImagePanel imagePanel = new ImagePanel();
            frame.setContentPane(imagePanel);

            JPanel controlPanel = new JPanel();
            JButton openButton = new JButton("Open Folder");
            openButton.addActionListener(e -> imagePanel.chooseDirectory());

            controlPanel.add(openButton);

            controlPanel.add(imagePanel.getStartButton());
            controlPanel.add(imagePanel.getStopButton());
            controlPanel.add(new JLabel("Delay (ms):"));
            JTextField delayField = new JTextField("3000", 5);
            delayField.addActionListener(e -> imagePanel.setDelay(Integer.parseInt(delayField.getText())));
            controlPanel.add(delayField);
            controlPanel.add(imagePanel.getStatusLabel());

            frame.add(controlPanel, BorderLayout.PAGE_START);

            frame.setVisible(true);
        });
    }
}