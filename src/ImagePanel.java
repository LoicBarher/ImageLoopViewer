import javax.swing.*;
import java.awt.*;
//import java.awt.event.*;
import java.io.File;
//import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
//import java.util.Arrays;

import static java.nio.file.StandardWatchEventKinds.*;

class ImagePanel extends JPanel {
    private File directory;
    private JLabel imageLabel;
    private File[] imageFiles;
    private int currentImageIndex = 0;
    private Timer timer;
    private WatchService watchService;
    private Thread watcherThread;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;

    public ImagePanel() {
        this.imageLabel = new JLabel();
        this.imageLabel.setHorizontalAlignment(JLabel.CENTER);
        setLayout(new BorderLayout());
        add(imageLabel, BorderLayout.CENTER);

        timer = new Timer(3000, e -> showNextImage());

        startButton = new JButton("Start");
        startButton.addActionListener(e -> startSlideshow());

        stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopSlideshow());
        stopButton.setEnabled(false);

        statusLabel = new JLabel("Status: Stopped");

        // Initialisations supplémentaires si nécessaire...
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getStopButton() {
        return stopButton;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public void chooseDirectory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (watcherThread != null && watcherThread.isAlive()) {
                watcherThread.interrupt(); // Interrupt the previous watcher thread
            }
            directory = fileChooser.getSelectedFile();
            setupDirectoryWatcher();
            loadImages();
            showNextImage();
            timer.start();
            updateControls();
        }
    }
    
    private void setupDirectoryWatcher() {
    if (watchService != null) {
        try {
            watchService.close();
        } catch (IOException e) {
            // This is expected if the watch service is already closed
        }
    }

    try {
        watchService = FileSystems.getDefault().newWatchService();
        Path path = directory.toPath();
        path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Error setting up WatchService: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        return; // Stop execution if the WatchService cannot be set up
    }

    watcherThread = new Thread(() -> {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take(); // This method can throw InterruptedException
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    System.out.println("Change detected: " + changed);
                    SwingUtilities.invokeLater(() -> {
                        loadImages();
                        showNextImage();
                    });
                }
                boolean valid = key.reset();
                if (!valid) {
                    break; // The key is no longer valid (the directory is inaccessible)
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Directory watching interrupted: " + e.getMessage());
        }
    });
    watcherThread.setDaemon(true);
    watcherThread.start();
}

    private void loadImages() {
        if (directory == null || !directory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "The selected path is not a directory or cannot be read.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File[] allFiles = directory.listFiles();
        if (allFiles == null || allFiles.length == 0) {
            statusLabel.setText("The selected folder is empty.");
            imageLabel.setText("The selected folder is empty.");
            startButton.setEnabled(false);
            return;
        }

        imageFiles = directory.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg");
        });

        if (imageFiles.length == 0) {
            statusLabel.setText("Folder does not contain any supported images.");
            imageLabel.setText("Folder does not contain any supported images.");
            startButton.setEnabled(false);
            handleNoImagesAvailable();
            return;
        }
       
        currentImageIndex = -1; // Start before the first image
        //startButton.setEnabled(true); // There are images to show

        System.out.println("Number of images loaded: " + (imageFiles != null ? imageFiles.length : "None"));
    }

    private void showNextImage() {
        if (imageFiles == null || imageFiles.length == 0) {
            handleNoImagesAvailable();
            return;
        } else if (imageFiles.length == 1) {
            handleOneImageAvailable();
            return;
        }
        
        // Si le fichier image n'existe pas, rechargez les images avant de continuer.
        currentImageIndex = (currentImageIndex + 1) % imageFiles.length;
        File imageFile = imageFiles[currentImageIndex];

        if (!imageFile.exists()) {
            loadImages();
            if (imageFiles == null || imageFiles.length == 0) {
                handleNoImagesAvailable();
                return;
            } else {
                // Si des images sont toujours présentes, montrez la prochaine image disponible.
                imageFiles = directory.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg");
                    });
                    if (imageFiles.length == 0) {
                        handleNoImagesAvailable();
                        return;
                    } else {
                        showNextImage();
                    return;
                }
            }
        }

        try {
            BufferedImage img = ImageIO.read(imageFile);
            ImageIcon imageIcon = new ImageIcon(img);
            Image scaledImage = imageIcon.getImage().getScaledInstance(imageLabel.getWidth(), imageLabel.getHeight(), Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.setText(""); // Clear any previous text
        } catch (IOException e) {
            // Si une erreur de lecture se produit, affichez un message d'erreur et passez à l'image suivante
            //JOptionPane.showMessageDialog(this, "Could not read image: " + imageFile.getName(), "Image Read Error", JOptionPane.ERROR_MESSAGE);
            //currentImageIndex++; // Passez à l'image suivante
            showNextImage();
        }
        System.out.println("Current image index: " + currentImageIndex + " of " + (imageFiles != null ? imageFiles.length : "None"));
    }

    public void startSlideshow() {
        if (directory == null) {
            JOptionPane.showMessageDialog(this, "Please select a folder first.", "No Folder Selected", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (imageFiles == null || imageFiles.length == 0) {
            JOptionPane.showMessageDialog(this, "The selected folder does not contain any pictures.", "No Pictures", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (timer != null && !timer.isRunning()) {
            timer.start();
            updateControls();
        }
    }

    private void handleNoImagesAvailable() {
        imageLabel.setIcon(null);
        imageLabel.setText("No images available in the selected directory.");
    }

    private void handleOneImageAvailable() {
        imageLabel.setIcon(null);
        imageLabel.setText("Not enough image available in the selected directory.");
    }

    /*private void updateControlsForNoImages() {
        // Mettre à jour les contrôles de l'interface utilisateur
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        // Autres mises à jour de l'interface utilisateur si nécessaire
    }*/

    private void stopSlideshow() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            updateControls();
        }
    }

    public void setDelay(int delay) {
        if (timer != null) {
            timer.setDelay(delay);
        }
    }

    private void updateControls() {
        if (timer.isRunning()) {
            statusLabel.setText("Status: Running");
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            statusLabel.setText("Status: Stopped");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }

    public void cleanup() {
        if (timer != null) {
            timer.stop();
        }
        if (watcherThread != null) {
            watcherThread.interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                // Handle exception if necessary
            }
        }
    }
}