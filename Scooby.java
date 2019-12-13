import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Scooby extends Application {

	public static void main(String[] args) {
		Application.launch(args);
	}

	static boolean playingRandom = false;
	static MediaPlayer baselinePlayer;
	static MediaPlayer randomPlayer;
	List<File> videos;
	Stack<File> currentVideos;
	final int EASTER_EGG_PERCENT = 3;

	
	
	@Override
	public void init() throws Exception {
		File file = new File("./videos/");
		if(file == null) {
			System.out.println("No videos folder found");
		}
		File[] allFiles = file.listFiles();
		videos = villainVideoList(allFiles);
	}

	@Override
	public void start(Stage primaryStage) {
		System.getProperties().setProperty("javafx.animation.framerate", "30");
		StackPane root = new StackPane();
		MediaView mediaView = new MediaView(getBaselineVideo());
		root.getChildren().add(mediaView);
		Scene scene = new Scene(root, 1024, 768, Color.BLACK);
		mediaView.fitWidthProperty().bind(scene.widthProperty());
		mediaView.fitHeightProperty().bind(scene.heightProperty());

		scene.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (!Scooby.playingRandom) {
				Scooby.playingRandom = true;
				getBaselineVideo().stop();
				Scooby.randomPlayer = getRandomVideo(mediaView);
				mediaView.setMediaPlayer(Scooby.randomPlayer);
				Scooby.randomPlayer.play();
			}
		});

		primaryStage.setScene(scene);
		primaryStage.show();
		getBaselineVideo().play();
	}
	
	public void resetVideos() {
		currentVideos = new Stack<File>();
		List<File> files = new ArrayList<File>(videos);
		Collections.shuffle(files);
		for(File file : files) {
			currentVideos.push(file);
		}
	}
	
	public File nextVideo() {
		if(currentVideos==null || currentVideos.isEmpty()) {
			resetVideos();
		}
		File videoToPlay = null;
		if(ThreadLocalRandom.current().nextInt(100) < EASTER_EGG_PERCENT) {
			videoToPlay = easterEggVideo();
		} 
		if(videoToPlay == null){ //no easter egg			
			videoToPlay = currentVideos.pop();
		}
		return videoToPlay;
	}

	private File easterEggVideo() {
		File[] allFiles = new File("./easterEgg/").listFiles();
		if(allFiles.length==0)
			return null;
		return allFiles[0];
	}

	private static MediaPlayer getBaselineVideo() {
		if (Scooby.baselinePlayer == null) {
			Scooby.baselinePlayer = new MediaPlayer(
					new Media(Scooby.class.getResource("baseline.mp4").toExternalForm()));
			Scooby.baselinePlayer.setCycleCount(MediaPlayer.INDEFINITE);
		}
		return Scooby.baselinePlayer;

	}

	private MediaPlayer getRandomVideo(MediaView mediaView) {
		File nextVideo = nextVideo();
		Scooby.randomPlayer = new MediaPlayer(
				new Media(Scooby.class.getResource(nextVideo.getPath()).toExternalForm()));
		randomPlayer.setOnEndOfMedia(new Runnable() {
			@Override
			public void run() {
				Scooby.randomPlayer.dispose();
				Scooby.playingRandom = false;
				mediaView.setMediaPlayer(getBaselineVideo());
				getBaselineVideo().play();

			}
		});
		return randomPlayer;
	}

	private List<File> villainVideoList(File[] allFiles) {
		List<File> videos = new ArrayList<File>();
		for (int i = 0; i < allFiles.length; i++)
			if (allFiles[i].getName().contains(".mp4"))
				videos.add(allFiles[i]);
		return videos;
	}

}