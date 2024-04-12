package GUI;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class MainController  {
    public Label redPx;
    public Label greenPx;
    public Label bluePx;
    public Label mixedPx;
    @FXML
    private ImageView imagePlace;
    @FXML
    private Button Prev;
    @FXML
    private Button nextBtn;
    @FXML
    private Label imgname;

    private List<Image> images = new ArrayList<>();
    private Timeline slideshowTimeline;
    private int currentIndex = 0;


    //Loads the image to the imageview
    public void loadImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            Task<Void> loadImageTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    for (File file : selectedFiles) {
                        Image image = new Image(file.toURI().toString());
                        images.add(image);
                        Platform.runLater(() -> {
                            imagePlace.setImage(image);
                            String name = file.getName();
                            imgname.setText(name);
                        });

                        // Start a new thread to count pixels for each image
                        Thread countPixelsThread = new Thread(() -> countPixels(file.getPath()));
                        countPixelsThread.start();
                    }
                    return null;
                }
            };
            new Thread(loadImageTask).start();
        }

    }



    //When the button is clicked,swithes image for next loaded.
    @FXML
    public void showNextImage(ActionEvent actionEvent) {
        if (!images.isEmpty()) {
            int currentIndex = images.indexOf(imagePlace.getImage());
            int nextIndex = (currentIndex + 1) % images.size();
            Image nextImage = images.get(nextIndex);
            imagePlace.setImage(nextImage);
            String name=nextImage.getUrl().substring(nextImage.getUrl().lastIndexOf("/")+1, nextImage.getUrl().length());
            imgname.setText(name);

            new Thread(() -> countPixels(nextImage)).start();

        }
    }

    //When the button is clicked,swithes image for previosly loaded.
    @FXML
    public void showPreviousImage(ActionEvent actionEvent) {
        if (!images.isEmpty()) {
            int currentIndex = images.indexOf(imagePlace.getImage());
            int previousIndex = (currentIndex - 1 + images.size()) % images.size();
            Image previousImage = images.get(previousIndex);
            imagePlace.setImage(previousImage);
            String name=previousImage.getUrl().substring(previousImage.getUrl().lastIndexOf("/")+1, previousImage.getUrl().length());
            imgname.setText(name);

            new Thread(() -> countPixels(previousImage)).start();
        }
    }

    //Logic for starting the slideshow.
    public void startSlideshow() {
        if (images.isEmpty()) {
            errorNoImages("No images loaded for slideshow.");
            return;
        }

        stopSlideshow();

        Task<Void> slideshowTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                slideshowTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
                    // Increment currentIndex before accessing the image
                    currentIndex = (currentIndex + 1) % images.size();
                    Image nextImage = images.get(currentIndex);
                    Platform.runLater(() -> {
                        imagePlace.setImage(nextImage);
                        String name = nextImage.getUrl().substring(nextImage.getUrl().lastIndexOf("/") + 1);
                        imgname.setText(name);
                    });

                    // Count pixels for the current image
                    countPixels(nextImage);
                }));
                slideshowTimeline.setCycleCount(Timeline.INDEFINITE);
                slideshowTimeline.play();
                return null;
            }
        };

        new Thread(slideshowTask).start();
    }

    private void countPixels(Image image) {
        Task<Void> countPixelsTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

                    int redCount = 0;
                    int greenCount = 0;
                    int blueCount = 0;
                    int mixedCount = 0;

                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();

                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int rgb = bufferedImage.getRGB(x, y);
                            int red = (rgb >> 16) & 0xFF;
                            int green = (rgb >> 8) & 0xFF;
                            int blue = rgb & 0xFF;

                            if (red > green && red > blue) {
                                redCount++;
                            } else if (green > red && green > blue) {
                                greenCount++;
                            } else if (blue > red && blue > green) {
                                blueCount++;
                            } else {
                                mixedCount++;
                            }
                        }
                    }

                    int finalRedCount = redCount;
                    int finalGreenCount = greenCount;
                    int finalBlueCount = blueCount;
                    int finalMixedCount = mixedCount;

                    Platform.runLater(() -> {
                        redPx.setText(String.valueOf(finalRedCount) + " Px");
                        greenPx.setText(String.valueOf(finalGreenCount) + " Px");
                        bluePx.setText(String.valueOf(finalBlueCount) + " Px");
                        mixedPx.setText(String.valueOf(finalMixedCount) + " Px");
                    });
                } catch (Exception e) {
                    e.printStackTrace(); // Handle or log the exception appropriately
                }
                return null;
            }
        };
        new Thread(countPixelsTask).start();
    }

    public void stopSlideshow() {
        if (slideshowTimeline != null) {
            slideshowTimeline.stop();
            slideshowTimeline = null;
        }
    }
public void countPixels(String imagePath) {
    int redCount = 0;
    int greenCount = 0;
    int blueCount = 0;
    int mixedCount=0;

    try {
        BufferedImage bufferedImage = ImageIO.read(new File(imagePath));

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = bufferedImage.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                if (red > green && red > blue) {
                    redCount++;
                } else if (green > red && green > blue) {
                    greenCount++;
                } else if (blue > red && blue > green) {
                    blueCount++;
                } else {
                    mixedCount++;
                }
            }
        }
        int finalRedCount = redCount;
        int finalGreenCount = greenCount;
        int finalBlueCount = blueCount;
        int finalMixedCount= mixedCount;
        Platform.runLater(() -> {
            redPx.setText(String.valueOf(finalRedCount) + " Px");
            greenPx.setText(String.valueOf(finalGreenCount) + " Px");
            bluePx.setText(String.valueOf(finalBlueCount) + " Px");
            mixedPx.setText(String.valueOf(finalMixedCount)+" Px");
        });
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

    private void errorNoImages(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No image loaded");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }
}
