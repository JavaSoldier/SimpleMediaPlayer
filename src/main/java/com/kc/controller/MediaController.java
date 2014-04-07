package com.kc.controller;

import com.kc.security.Security;
import com.kc.service.MediaControl;
import com.kc.service.MediaControlHide;
import com.kc.service.WarningDialog;
import com.kc.utils.MyFileUtils;
import com.kc.utils.PropertiesUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MediaController extends Application implements Initializable {

    private MediaPlayer mediaPlayer;
    private static MediaView mediaView;
    public static Stage primaryStage;
    private static Scene scene;
    public static ObservableList<File> tempList = FXCollections
            .observableArrayList();
    public static BorderPane root;
    public static VBox box = new VBox();
    public MenuBar menuBar;
    private static MediaControl mediaControl;
    private static Stage mediaControlStage;
    private static StackPane stackPane;
    private Executor executor;

    @Override
    public void initialize(URL paramURL, ResourceBundle paramResourceBundle) {

    }

    @Override
    public void start(final Stage primaryStage) throws Exception {

        try {
            MediaController.primaryStage = primaryStage;
            primaryStage.setTitle("Media Player");
            FXMLLoader loader = new FXMLLoader(
                    MediaController.class
                            .getResource("/com/kc/view/mediaControl.fxml"));
            root = (BorderPane) loader.load();
            menuBar = (MenuBar) root.getTop();
            // creating Space for media
            HBox rectangle = new HBox();
            rectangle.setPrefSize(500, 300);
            root.setCenter(rectangle);

            // Controls at Bottom
            mediaControl = new MediaControl(
                    (MediaController) loader.getController());
            root.setBottom(mediaControl);

            final Scene scene = new Scene(root);
            MediaController.scene = scene;
            MediaController.scene.getStylesheets().add(
                    MediaController.class.getResource(
                            "/com/kc/style/MediaPlayer.css").toExternalForm());
            MediaController.primaryStage.setScene(MediaController.scene);
            MediaController.primaryStage.show();

            executor = Executors.newCachedThreadPool();

            scene.widthProperty().addListener(new ChangeListener<Number>() {

                @Override
                public void changed(
                        ObservableValue<? extends Number> observable,
                        Number oldValue, Number newValue) {
                    if (null != mediaView)
                        mediaView.setFitWidth(newValue.doubleValue());
                }
            });

            scene.heightProperty().addListener(new ChangeListener<Number>() {

                @Override
                public void changed(
                        ObservableValue<? extends Number> observable,
                        Number oldValue, Number newValue) {
                    // Subtracting height of MediaControl & Menu on top
                    if (null != mediaView) {
                        if (!primaryStage.isFullScreen())
                            mediaView.setFitHeight(newValue.doubleValue() - 25 - 45);
                        else
                            mediaView.setFitHeight(newValue.doubleValue());
                    }
                }
            });

            scene.addEventFilter(MouseEvent.MOUSE_PRESSED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {

                            if (mouseEvent.getButton().equals(
                                    MouseButton.PRIMARY)) {
                                if (mouseEvent.getClickCount() == 2) {
                                    if (primaryStage.isFullScreen()) {
                                        root.setTop(menuBar);
                                        root.setBottom(mediaControl);
                                        stackPane.getChildren().clear();
                                        scene.setRoot(root);
                                        primaryStage.setFullScreen(false);

                                    } else {
                                        if (!(null == mediaPlayer)) {
                                            root.setTop(new VBox());
                                            root.setBottom(new HBox());
                                            stackPane = new StackPane();
                                            stackPane.getChildren().addAll(
                                                    root, mediaControl);
                                            scene.setRoot(stackPane);
                                            primaryStage.setFullScreen(true);
                                            StackPane.setMargin(
                                                    mediaControl,
                                                    new Insets(scene
                                                            .getHeight() - 35,
                                                            0, 10, 0));
                                            MediaControlHide command = new MediaControlHide(
                                                    primaryStage, mediaControl);
                                            executor.execute(command);
                                        }
                                    }
                                }
                            }
                        }
                    });

            scene.setOnDragOver(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    Dragboard db = event.getDragboard();
                    if (db.hasFiles()) {
                        event.acceptTransferModes(TransferMode.COPY);
                    } else {
                        event.consume();
                    }
                }
            });
            scene.setOnDragDropped(new EventHandler<DragEvent>() {

                @Override
                public void handle(DragEvent event) {

                    Dragboard db = event.getDragboard();
                    if (db.hasFiles()) {
                        String filePath;
                        tempList.clear();
                        for (File file : db.getFiles()) {
                            try {
                                if (PropertiesUtils.readFormats().contains("*" + file.getAbsolutePath().substring(file.getAbsolutePath().length() - 4))) {
                                    tempList.add(file);
                                    filePath = file.getAbsolutePath();
                                    if (null != mediaPlayer)
                                        mediaPlayer.stop();
                                    mediaControl.resetPlayList(tempList);
                                    playVideo(filePath);
                                } else {
                                    WarningDialog.showWarning(primaryStage);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            });

            scene.addEventFilter(MouseEvent.ANY,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent arg0) {
                            if (primaryStage.isFullScreen()) {
                                mediaControl.setOpacity(1.0);
                            }
                        }
                    });

            scene.addEventFilter(KeyEvent.KEY_PRESSED,
                    new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent t) {
                            if (t.getCode() == KeyCode.ESCAPE) {
                                if (((HBox) root.getBottom()).getChildren()
                                        .size() == 0) {
                                    root.setTop(menuBar);
                                    root.setBottom(mediaControl);
                                    stackPane.getChildren().clear();
                                    scene.setRoot(root);
                                    primaryStage.setFullScreen(false);
                                    mediaView.setFitHeight(scene.getHeight() - 25 - 40);
                                }
                            }
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playVideo(String MEDIA_URL) {
        try {
            MEDIA_URL = URLEncoder.encode(MEDIA_URL, "UTF-8");
            MEDIA_URL = "file:/"
                    + (MEDIA_URL).replace("\\", "/").replace("+", "%20");
            Media media = new Media(MEDIA_URL);
            // create media player
            mediaPlayer = new MediaPlayer(media);
            mediaControl.setMediaPlayer(mediaPlayer);
            mediaView = new MediaView(mediaPlayer);
            mediaControl.setMediaView(mediaView);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.play();
            mediaView.setPreserveRatio(false);
            mediaView.autosize();

            root.setCenter(mediaView);

            mediaView.setFitHeight(scene.getHeight() - 25 - 40);
            mediaView.setFitWidth(scene.getWidth());
            MediaControl.volButton.setSelected(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        primaryStage.close();
//        Path target = Paths.get("C:\\tempFolder");
//        try {
//            MyFileUtils.dellDir(target);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }


    public static void main(String[] args) throws IOException {
        try {
            Security.securityAction();
            launch(args);
        } catch (Exception e) {
            Security.log(e);
            System.exit(0);
        }
    }

}
