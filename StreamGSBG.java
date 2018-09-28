package bbb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class StreamGSBG implements Runnable{
	// Handles getting images from users to put as a background for a greenscreen
	
	private static int cost = 1000;
	public static ArrayList<String> images = new ArrayList<String>();
	private static String dirPath = null;
	
	public void run() {
		while(TwitchChat.connected) {
			if(images.size() > 0 && GUIHandler.approval) {
				GUIHandler.approval = false;
				String urlStr = images.get(0);
				images.remove(0);
				StreamMessage.playSound();
				// Image downloading from https://stackoverflow.com/a/5882039
				BufferedImage image = null;
				try {
				    URL url = new URL(urlStr);
				    image = ImageIO.read(url);
				    if (dirPath == null) {
					    Path currentRelativePath = Paths.get("");
					    dirPath = currentRelativePath.toAbsolutePath().toString();
				    }
				    File imageFile = new File(dirPath + "/gsbg/current.png");
				    ImageIO.write(image, "png", imageFile);
				} catch (IOException e) {
					System.err.println(e);
					if (dirPath != null)
						System.err.println(dirPath);
				}
			}
			else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void add(String user, String message) {
		if(PlayersHandler.getPoints(user) >= cost) {
			// message looks like "!buygsbg https://www.imgur.com/meme.jpg"
			String rawURL = message.substring(9);
			if(rawURL.contains("http://") || rawURL.contains("https://")) {
				PlayersHandler.removePoints(user, cost);
				System.out.println("Attempted GSBG from " + user + " with " + message.substring(9));
				images.add(message.substring(9));
				TwitchChat.outsideMessage(user + " added " + message.substring(9));
				TwitchChat.outsidePM(user, "Your background has been added "+
							"to the queue, " + user + ".");
			}
			else {
				TwitchChat.outsidePM(user, "Sorry, " + user + ", but that "+
						"URL looks weird D: I expect something like " +
						"'https://i.imgur.com/meme.jpg'");
			}
			
		}
		else {
			TwitchChat.outsideMessage("Sorry, " + user + ", but it "+
						"it costs " + cost + " points to buy a background.");
		}
	}
}
