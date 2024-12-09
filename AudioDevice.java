/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
 * -- Project Simon -------------------------------------------------------------*
 * -- Start Date: 23rd November, 2022 -------------------------------------------*
 * -- Last Update: 1st December, 2022 -------------------------------------------*
 * -- Hridyanshu Aatreya <2200096@brunel.ac.uk> ---------------------------------*
 * -- Yellow 46 -----------------------------------------------------------------*
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
 * -- The primary handler for all audio-related operations. This class contains -*
 * -- useful methods such as play() and stop(), that are frequently used in the -*
 * -- Classic Engine. -----------------------------------------------------------*
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioDevice {
	
	static AudioInputStream audioStream;
	static Clip clip;
	
	final static String HOME_DIR = "tracks/";
	
	public static void play(String trackName, boolean loopOrNot)
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		
		audioStream = AudioSystem.getAudioInputStream(new File(HOME_DIR + trackName).getAbsoluteFile());
		
		clip = AudioSystem.getClip();
		clip.open(audioStream);
		if (loopOrNot) clip.loop(Clip.LOOP_CONTINUOUSLY);
		
		clip.start();
	}
	
	public static void stop() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		clip.stop();
		clip.close();
	}
}
