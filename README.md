# ImageLoopViewer

## Description
ImageLoopViewer is a Java application that allows users to view a slideshow of images from a local directory. It loads all images from the selected folder and displays them in a loop on full screen.

## Features
- Selection of a local directory containing images.
- Full-screen display of images in an infinite loop.
- Automatic slide show with adjustable intervals.
- Dynamic updates when images are added, removed, or modified in the folder.

## Requirements
- This application requires Java Runtime Environment (JRE) version 17 or higher to run.

## Installation
Download the `ImageLoopViewer.jar` file.

### Running the Application with the Custom Java Runtime
If you do not have JRE version 17 or higher installed, or you prefer not to update your Java installation, you can use the custom Java runtime image provided with the application.

1. Download the `jre.zip` file.
2. Extract the `jre.zip` to a known location on your system.
3. Navigate to the extracted `jre` directory and run the application with the following command:

   On Windows:

```bash
.\bin\java -jar path\to\ImageLoopViewer.jar
```
Note: Replace `path\to` with the actual path to the `ImageLoopViewer.jar` file.

## Technical Overview
### Classes and Methods
- MainApp: The entry point of the application. It creates the main application frame and initializes the ImagePanel.
- ImagePanel: A custom JPanel that handles the display of images, directory selection, and slideshow functionality.
- chooseDirectory(): Opens a dialog for directory selection.
- loadImages(): Loads all image files from the selected directory.
- showNextImage(): Displays the next image in the panel.
- startSlideshow(): Starts the automatic slideshow.
- stopSlideshow(): Stops the slideshow.
### Threads
The application uses a separate watcherThread to monitor the selected directory for changes using WatchService. This allows the slideshow to update dynamically when files are added, deleted, or changed.

## License
This project is licensed under the MIT License. See the LICENSE file for more information.

## Contribution
Contributions are welcome! If you would like to contribute, please fork the repository and submit your pull requests.

## Support
If you encounter any issues or have questions, feel free to open an issue.
