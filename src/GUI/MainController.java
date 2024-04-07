package GUI;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class MainController  {
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
            for (File file : selectedFiles) {
                Image image = new Image(file.toURI().toString());
                images.add(image);
                imagePlace.setImage(image);
                String name= file.getName();
                imgname.setText(name);
            }
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
        }
    }

    //Logic for starting the slideshow.
    public void startSlideshow(){
        if (images.isEmpty()) {
            errorNoImages("No images loaded for slideshow.");
            return;
        }

        stopSlideshow();

        slideshowTimeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            currentIndex = (currentIndex + 1) % images.size();
            Image nextImage = images.get(currentIndex);
            imagePlace.setImage(nextImage);
            String name=nextImage.getUrl().substring(nextImage.getUrl().lastIndexOf("/")+1, nextImage.getUrl().length());
            imgname.setText(name);
        }));
        slideshowTimeline.setCycleCount(Timeline.INDEFINITE);
        slideshowTimeline.play();
    }

    public void stopSlideshow() {
        if (slideshowTimeline != null) {
            slideshowTimeline.stop();
            slideshowTimeline = null;
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
