/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
 * -- Project Simon -------------------------------------------------------------*
 * -- Start Date: 23rd November, 2022 -------------------------------------------*
 * -- Last Update: 1st December, 2022 -------------------------------------------*
 * -- Hridyanshu Aatreya <2200096@brunel.ac.uk> ---------------------------------*
 * -- Yellow 46 -----------------------------------------------------------------*
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
 * -- Bootstrapper code that starts up the game loop. The actual game functions -*
 * -- are processed in the ClassicEngine class. ---------------------------------*
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import swiftbot.SwiftBotAPI;

public class Simon {
	
	static SwiftBotAPI swiftBot;
	public static final Scanner in = new Scanner(System.in);
	
	static boolean gameStateSwitch;
	
	// ANSI Color Code & Escape Sequence character definitions
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	
	static final String CLEAR_TERM = "\033[2J";
	static final String POSITION_CURSOR_TOP = "\033[0;0H";
	static final String ITALICS_START = "\033[3m";
	static final String ITALICS_END = "\033[0m";
	
	public static final long RENDER_TIME = 150L;
	
	public static void main(String[] args) 
			throws InterruptedException, UnsupportedAudioFileException, IOException, LineUnavailableException {
		
		try {
			swiftBot = new SwiftBotAPI();
		} catch(Exception e) {
			// Outputs an I2C warning if the module is disabled.
			System.out.println("\nI2C disabled!");
			System.out.println("Run the following command:");
			System.out.println("sudo raspi-config nonint do_i2c 0\n");
			System.exit(0);
		}
		
		/*** GAME LOADING SEQUENCE ***/
		LoadingSlash();
		
		gameIntro();
		
		gameSequence();
		
		swiftBot.shutdown();
		
		/* As of 28th November, 2022, I'm not certain as to what's keeping the program from completely
		 * exiting. Thus, I have to resort to this technique of manually terminating the JVM.
		 */
		System.exit(0);
	}
	
	private static void gameIntro() 
			throws InterruptedException, UnsupportedAudioFileException, IOException, LineUnavailableException {
		renderText("RAM OK", RENDER_TIME, ANSI_CYAN);
		renderText("ROM OK\n", RENDER_TIME, ANSI_CYAN);
			
		AudioDevice.play("intro.wav", true);
		
		renderText("Yellow 46 presents\n", RENDER_TIME, ANSI_YELLOW);
		renderText("An implementation by Hridyanshu Aatreya\n", RENDER_TIME, ANSI_WHITE);
		
		// Game initiation sequence
		System.out.println(" /*************************************\n"
			+ ANSI_YELLOW
			+ "   _____ _                             \n"
			+ "  / ___/(_)___ ___  ____  ____         \n"
			+ "  \\__ \\/ / __ `__ \\/ __ \\/ __ \\        \n"
			+ " ___/ / / / / / / / /_/ / / / /        \n"
			+ "/____/_/_/ /_/ /_/\\____/_/ /_/         \n"
			+ "                / ___/____ ___  _______\n"
			+ "                \\__ \\/ __ `/ / / / ___/\n"
			+ "               ___/ / /_/ / /_/ (__  ) \n"
			+ "              /____/\\__,_/\\__, /____/  \n"
			+ "                         /____/        \n"
			+ ANSI_RESET
			+ " ======================================\n"
			+ " Version 1.0-64\n"
			+ " Written by Hridyanshu Aatreya\n"
			+ " <2200096@brunel.ac.uk>\n"
			+ " *************************************/\n");
	}
	
	/* This method accepts a string and gradually prints it to the terminal similar to classic text
	 * adventure games from the '80s 
	 */
	private static void renderText(String text, long delay, String ANSI_COLOR)
			throws InterruptedException {
		
		for(int i = 0; i < text.length(); ++i) {
			if(i == (text.length() -1)) {
				System.out.print(ANSI_COLOR + text.charAt(i) + "\n" + ANSI_RESET);
				Thread.sleep(delay);
			}
			else {
				System.out.print(ANSI_COLOR + text.charAt(i) + ANSI_RESET);
				Thread.sleep(delay);
			}
		}
	}
	
	// Fancy slash loading method that lasts for a couple seconds before the game introduction sequence.
	private static void LoadingSlash() 
			throws InterruptedException {
		
		String slash = "|/-\\";
		
		System.out.print(CLEAR_TERM); // Clear character
		long startPoint = System.currentTimeMillis();
		
		while (true) {
			for (int i = 0; i < 4; ++i) {
				System.out.print(CLEAR_TERM);
				System.out.print(POSITION_CURSOR_TOP);
				
				for (int j = 0; j < 1; ++j) {
					System.out.print(slash.charAt(i));
				}
				Thread.sleep(250);
			}
			long currentPoint = System.currentTimeMillis();
			if((currentPoint - startPoint) >= 5000) break;
		}
		System.out.print(CLEAR_TERM);
		System.out.print(POSITION_CURSOR_TOP);
	}

	/* The main game loop method that registers whether the game should continue running. It includes
	 * several sub methods that start up the Classic Engine.
	 */
	private static void gameSequence() 
			throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
		
		// Loop variable
		String choice = "y";
		
		// Main game loop
		while ((choice.compareTo("Y") == 0) || (choice.compareTo("y") == 0)) {
			game();
			
			// Store player's decision to continue the game or not.
			boolean shouldGameContinue = gameContinue();
			
			if (shouldGameContinue) {
				choice = "y";	
			}
			else {
				break;
			}
		}
	}

	// A sub-method to gameSequence() that is responsible for firing up the Classic Engine.
	private static void game() 
			throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
		
		System.out.println(ITALICS_START + "\nPress ENTER to start the game!" + ITALICS_END);
		in.nextLine();
		
		System.out.println("Starting game...\n");
		AudioDevice.stop();
		Thread.sleep(150L);
		
		ClassicEngine gameEngine = new ClassicEngine();
		gameEngine.gameProcess(swiftBot);
	}
	
	// This method receives input from the user on whether they wish to play again.
	private static boolean gameContinue() 
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		
		gameStateSwitch = false;
		String choice;
		
		System.out.println("Try again?[y/n]");
		System.out.print("> ");
		choice = in.nextLine();
		
		if ((choice.compareTo("N") == 0) || (choice.compareTo("n") == 0)) {
			AudioDevice.stop();
			System.out.println("See you next time!");
			gameStateSwitch = false;
			
			return gameStateSwitch;
		}
		else if((choice.compareTo("Y") == 0) || (choice.compareTo("y") == 0)) {
			gameStateSwitch = true;
			AudioDevice.stop();
			
			return gameStateSwitch;
		}
		else {
			System.out.println(ANSI_RED + "ERROR! " + ANSI_RESET + "Invalid input detected, try again.\n");
			gameContinue();
		}
		
		return gameStateSwitch;
	}
}

