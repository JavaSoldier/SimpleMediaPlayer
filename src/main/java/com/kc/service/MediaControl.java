package com.kc.service;

import com.kc.controller.MediaController;
import com.kc.utils.PropertiesUtils;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MediaControl extends HBox {

    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private final boolean repeat = false;
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private Duration duration;
    private SlidoBar timeSlider;
    private Label playTime;
    private SlidoBar volumeSlider;
    private Button playButton;
    private Button stopButton;
    private Button playListButton;
    public static ToggleButton volButton;
    private ToggleGroup group;
    private VBox listBox;
    private ListView<File> playList;
    //	private Button ;add
//	private Button remove;
    private MediaController mediaController;
    private Stage stage;
    double prevVolStatus = 1;
    String currentVideo = "";
    private ObservableList<File> fileList = FXCollections.observableArrayList();

    public MediaControl(final MediaController mediaController) {

        this.mediaController = mediaController;
        setStyle("-fx-background-color: #3E3E3E;");
        setId("control-bar");
        setPrefHeight(50);
        setMinHeight(50);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(5, 10, 5, 10));

        playButton = new Button();
        playButton.setId("play");
        stopButton = new Button();
        stopButton.setId("stop");
        playListButton = new Button();
        playListButton.setId("playlist");
        listBox = new VBox(10);
        playList = new ListView<File>();
        initPlayList();
        HBox box = new HBox();
        box.setPadding(new Insets(0, 5, 5, 5));
        box.setAlignment(Pos.CENTER);
        listBox.getChildren().addAll(playList, box);
        final Scene scene = new Scene(listBox);
        scene.getStylesheets().add(
                MediaControl.class.getResource("/com/kc/style/MediaPlayer.css")
                        .toExternalForm()
        );

        playListButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                try {
                    Platform.runLater(new Runnable() {
                        public void run() {

                            stage = new Stage();
                            stage.setScene(scene);
                            stage.show();

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        getChildren().add(playButton);
        getChildren().add(stopButton);

        // Add Time label
        Label timeLabel = new Label("Time: ");
        timeLabel.setId("timeLabel");
        timeLabel.setMinWidth(40);
        getChildren().add(timeLabel);

        // Add time slider
        timeSlider = new SlidoBar();
        HBox.setHgrow(timeSlider, Priority.ALWAYS);
        timeSlider.setMinWidth(50);
        timeSlider.setMaxWidth(Double.MAX_VALUE);
        timeSlider.getSlider().valueProperty()
                .addListener(new InvalidationListener() {
                    public void invalidated(Observable ov) {
                        if (timeSlider.getSlider().isValueChanging()) {
                            if (null != mediaPlayer)
                                // multiply duration by percentage calculated by
                                // slider position
                                mediaPlayer.seek(duration.multiply(timeSlider
                                        .getSlider().getValue() / 100.0));
                            else
                                timeSlider.getSlider().setValue(0);
                        }
                    }
                });

        getChildren().add(timeSlider);

        // Add Play label
        playTime = new Label(" 00:00:00/00:00:00");
        playTime.setId("timeLabel");
        playTime.setPrefWidth(130);
        playTime.setMinWidth(100);
        getChildren().add(playTime);

        // Add the Playlist
        getChildren().add(playListButton);

        // Add the volume label
        group = new ToggleGroup();
        volButton = new ToggleButton();
        volButton.setToggleGroup(group);
        volButton.setId("volume");
        getChildren().add(volButton);
        group.selectedToggleProperty().addListener(
                new ChangeListener<Toggle>() {

                    @Override
                    public void changed(
                            ObservableValue<? extends Toggle> observable,
                            Toggle oldValue, Toggle newValue) {

                        if (oldValue != null) {
                            mediaPlayer.setVolume(0.5);
                        } else if (newValue != null) {
                            mediaPlayer.setVolume(0);
                        }

                    }
                }
        );

        // Add Volume slider
        volumeSlider = new SlidoBar();
        volumeSlider.setPrefWidth(70);
        volumeSlider.setMaxWidth(Region.USE_PREF_SIZE);
        volumeSlider.setMinWidth(70);
        HBox.setHgrow(volumeSlider, Priority.ALWAYS);
        volumeSlider.getSlider().valueProperty()
                .addListener(new InvalidationListener() {
                    public void invalidated(Observable ov) {
                        if (volumeSlider.getSlider().isValueChanging()) {
                            if (null != mediaPlayer) {
                                // multiply duration by percentage calculated by
                                // slider position
                                if (volumeSlider.getSlider().getValue() > 0) {
                                    volButton.setSelected(false);
                                } else if (volumeSlider.getSlider().getValue() == 0) {
                                    volButton.setSelected(true);
                                }
                                mediaPlayer.setVolume(volumeSlider.getSlider()
                                        .getValue() / 100.0);
                                prevVolStatus = volumeSlider.getSlider()
                                        .getValue() / 100.0;
                            } else
                                volumeSlider.getSlider().setValue(0);

                        }
                    }
                });

        getChildren().add(volumeSlider);

        playList.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2) {
                    if (playList.getSelectionModel().getSelectedItem() != null) {
                        playButton.setDisable(false);
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                        }
                        mediaController.playVideo(playList.getSelectionModel()
                                .getSelectedItem().getAbsolutePath());
                        volButton.setSelected(false);
                        currentVideo = playList.getSelectionModel()
                                .getSelectedItem().getAbsolutePath();
                    }
                }

            }
        });

        playList.setOnDragOver(new EventHandler<DragEvent>() {
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
        playList.setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {

                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    for (File file : db.getFiles()) {
                        try {
                            if (PropertiesUtils.readFormats().contains("*" + file.getAbsolutePath().substring(file.getAbsolutePath().length() - 4))) {
                                MediaController.tempList.add(file);
                            } else {
                                // initialize the confirmation dialog
                                WarningDialog.showWarning(stage);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    playList.setItems(MediaController.tempList);
                }
            }
        });

        playList.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {

            @Override
            public ListCell<File> call(ListView<File> p) {

                ListCell<File> cell = new ListCell<File>() {

                    @Override
                    protected void updateItem(File t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t != null) {
                            setText(t.getName());
                        }
                    }

                };

                return cell;
            }
        });

    }

    private void initPlayList() {
        try {
            List<String> nameList = parseNames(getClass().getResourceAsStream("/text/names.txt"));
            Path target = Paths.get("C:\\tempFolder");

            File[] videoFiles = new File(target.toUri()).listFiles();
            if (videoFiles == null || videoFiles.length == 0) {

                Path newDirPath = Files.createDirectories(target);
                Files.setAttribute(newDirPath, "dos:hidden", true);
                videoFiles = hackThisShit(getStreamList(nameList), nameList);
            }

            if (videoFiles != null) {
                for (File file : videoFiles) {
                    if (file.isFile()) {
                        MediaController.tempList.add(file);
                    }
                }
            }
            playList.setItems(MediaController.tempList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> parseNames(InputStream stream) throws IOException {
        List<String> nameList = new ArrayList();
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNextLine()) {
            nameList.add(scanner.nextLine());
        }
        scanner.close();
        stream.close();
        return nameList;
    }

    private List<InputStream> getStreamList(List<String> nameList) {
        List<InputStream> streamList = new ArrayList();
        for (String name : nameList) {
            String trimName = name.trim();
            streamList.add(getClass().getResourceAsStream("/videoFolder/" + trimName));
        }
        return streamList;
    }

    private File[] hackThisShit(List<InputStream> streamList, List<String> nameList) throws IOException {
        File[] fileList = new File[streamList.size()];
        for (int i = 0; i < streamList.size(); i++) {
            File file = new File("C:\\tempFolder\\" + nameList.get(i));
            InputStream stream = streamList.get(i);
            OutputStream out = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;
            while ((len = stream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            stream.close();
            fileList[i] = file;
        }
        return fileList;
    }

    protected void updateValues() {
        if (playTime != null && timeSlider != null && volumeSlider != null) {
            Platform.runLater(new Runnable() {
                public void run() {
                    Duration currentTime = mediaPlayer.getCurrentTime();
                    playTime.setText(" " + formatTime(currentTime, duration));
                    timeSlider.setDisable(duration.isUnknown());
                    if (!timeSlider.isDisabled()
                            && duration.greaterThan(Duration.ZERO)
                            && !timeSlider.getSlider().isValueChanging()) {
                        timeSlider
                                .getSlider()
                                .setValue(
                                        currentTime.divide(duration).toMillis() * 100.0);
                    }
                    if (!volumeSlider.getSlider().isValueChanging()) {
                        volumeSlider.getSlider()
                                .setValue(
                                        (int) Math.round(mediaPlayer
                                                .getVolume() * 100)
                                );
                    }
                }
            });
        }
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        int elapsedMinutes = intElapsed / 60 - elapsedHours * 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            int durationMinutes = intDuration / 60 - durationHours * 60;
            int durationSeconds = intDuration - durationHours * 60 * 60
                    - durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds, durationHours,
                        durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d", elapsedMinutes,
                        elapsedSeconds, durationMinutes, durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }

    public void setMediaPlayer(final MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        mediaPlayer.currentTimeProperty().addListener(
                new InvalidationListener() {
                    public void invalidated(Observable ov) {
                        updateValues();
                    }
                }
        );

        mediaPlayer.setOnPlaying(new Runnable() {
            public void run() {
                if (stopRequested) {
                    mediaPlayer.pause();
                    stopRequested = false;
                } else {
                    playButton.setId("pause");
                }
            }
        });

        mediaPlayer.setOnPaused(new Runnable() {
            public void run() {
                System.out.println("onPaused");
                playButton.setId("play");
            }
        });

        mediaPlayer.setOnReady(new Runnable() {
            public void run() {
                duration = mediaPlayer.getMedia().getDuration();
                updateValues();
            }
        });

        mediaPlayer.setCycleCount(repeat ? MediaPlayer.INDEFINITE : 1);
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            public void run() {
                if (!repeat) {
                    playButton.setId("play");
                    stopRequested = true;
                    atEndOfMedia = true;
                }
            }
        });

        playButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Status status = mediaPlayer.getStatus();

                if (status == Status.UNKNOWN || status == Status.HALTED) {
                    // don't do anything in these states
                    return;
                }

                if (status == Status.PAUSED || status == Status.READY
                        || status == Status.STOPPED) {
                    // rewind the movie if we're sitting at the end
                    if (atEndOfMedia) {
                        mediaPlayer.seek(mediaPlayer.getStartTime());
                        atEndOfMedia = false;
                    }
                    mediaPlayer.play();
                } else {
                    mediaPlayer.pause();
                }
            }
        });
        stopButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {

                mediaPlayer.stop();
                playButton.setId("play");

            }
        });

    }

    public void setMediaView(MediaView mediaView) {
        this.mediaView = mediaView;
    }

    public void resetPlayList(ObservableList<File> list) {
        playList.setItems(list);
    }

    public SlidoBar getTimeSlider() {
        return this.timeSlider;
    }

}