/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
 * -- Project Simon -------------------------------------------------------------------*
 * -- Start Date: 23rd November, 2022 -------------------------------------------------*
 * -- Last Update: 1st December, 2022 -------------------------------------------------*
 * -- Hridyanshu Aatreya <2200096@brunel.ac.uk> ---------------------------------------*
 * -- Yellow 46 -----------------------------------------------------------------------*
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
 *-- The Classic Engine handles all gameplay elements once the bootstrapper code ------*
 *-- in the main method passes over program control to CE. The engine interfaces with -*
 *-- the SwiftBot API and makes it easier to handle low-level operations. -------------*
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import swiftbot.SwiftBotAPI;
import swiftbot.SwiftBotAPI.Underlight;

public class ClassicEngine {
	ClassicEngine() throws InterruptedException {
	}

	// RGB color definitions as constants for use later in the program.
	// N.B. CE stands for 'Classic Engine'.
	static final int[] CE_RED = {255, 0, 0};
	static final int[] CE_GREEN = {0, 255, 0};
	static final int[] CE_BLUE = {0, 0, 255};
	static final int[] CE_YELLOW = {255, 255, 0};
	
	static final String CE_POSITION_CURSOR_TOP = "\033[0;0H";
	
	// Step ArrayList & index declarations
	static ArrayList<Step> stepArray = new ArrayList<Step>();
	static int currentStepIndex = 0;
	static boolean allStepsCorrect;
	static boolean continueAllowed = false;
	
	// Magnitude by which extra time is to be compensated to the player.
	static int timeMagnitude = 1;
	
	// Player score and points earned per round
	static int playerScore;
	static int playerScorePoints;
	
	// Session score variables
	static int sessionHighScore;
	static boolean playedOnce = false;
	
	// Classic Engine's raison d'etre: The method where actual gameplay elements are processed.
	public void gameProcess(SwiftBotAPI swiftBot) 
			throws InterruptedException, UnsupportedAudioFileException, IOException, LineUnavailableException {
		
		int playerLives = 3;
		
		// A loop to continue the game until player runs out of lives
		while (playerLives > 0) {
			System.out.println(Simon.CLEAR_TERM);
			System.out.println(Simon.POSITION_CURSOR_TOP);
			System.out.println("Player Lives: " + playerLives);
			System.out.println("Score: " + playerScore + "\n");
			
			createStep();
			displaySteps(swiftBot);
			
			boolean playerMove = awaitPlayerInput(swiftBot);
			playerScore += playerScorePoints;
			
			/* Should the playerMove have yielded true, the player has won the round and can be granted
			 * a certain victory along with their points earned and some Nintendo or non-copyrighted music.
			 * Otherwise, deduct a life and play some more Nintendo or non-copyrighted music to signify a
			 * defeat.
			 * Additionally, the SwiftBot "dances" whenever the player gets all steps correct.
			 */
			if (playerMove) {
				AudioDevice.stop();
				System.out.println(Simon.ANSI_GREEN + "\nYou Won!\n" + Simon.ANSI_RESET);
				AudioDevice.play("victory.wav", false);
				
				victoryDance(swiftBot);

				Thread.sleep(2000);
			}
			else {
				AudioDevice.stop();
				System.out.println("You lost a life!\n");
				AudioDevice.play("defeat.wav", false);
				playerLives -= 1;
				
				Thread.sleep(3000);
			}
			
			// Reset all temporary score points and compensated time values.
			currentStepIndex = 0;
			playerScorePoints = 0;
			timeMagnitude = 1;
			AudioDevice.stop();
		}
		
		
		System.out.println(" /*****************************************************\n" 
				+ Simon.ANSI_RED 
				+"   _________    __  _________   ____ _    ____________ \n"
				+ "  / ____/   |  /  |/  / ____/  / __ \\ |  / / ____/ __ \\\n"
				+ " / / __/ /| | / /|_/ / __/    / / / / | / / __/ / /_/ /\n"
				+ "/ /_/ / ___ |/ /  / / /___   / /_/ /| |/ / /___/ _, _/ \n"
				+ "\\____/_/  |_/_/  /_/_____/   \\____/ |___/_____/_/ |_|  \n"
				+ "                                                       \n"
				+ Simon.ANSI_RESET
				+ " *****************************************************/\n");
		
		displayPostGameResults();
		
		/* Post-game cleanup routine - reset all score & step values to their original values in case the
		 * player chooses to come back for another spin
		 */
		stepArray.clear();
		playerScore = 0;
		swiftBot.disableUnderlights();
		
	}
	
	/* Creates a single step object to be added into the stepArray that can be read by the
	 * displayStep() method.
	 */
	private static ArrayList<Step> createStep() {
		
		// Permitted values to be added into a Step object.
		String[] colors = {"R", "G", "B", "Y"};
		int[] points = {20, 50, 100};
		
		Random rand = new Random();
		
		int COLORS_MAX = 3;
		int MULTI_MAX = 2;
		
		// Create random values based off of given parameters
		int newColorElement = rand.nextInt(COLORS_MAX - 0 + 1) + 0;
		int newPointsElement = rand.nextInt(MULTI_MAX - 0 + 1) + 0;

		Step nextStep = new Step(colors[newColorElement], points[newPointsElement]);
		stepArray.add(nextStep);
		
		return stepArray;
	}
	
	/* Display the generated step values from the stepArray. The for loop uses certain internal methods
	 * to translate String values into those accepted by the SwiftBot API: in this case, the LED and RGB color
	 * values.
	 */
	private static void displaySteps(SwiftBotAPI swiftBot) {
		try {
			for (Step s: stepArray) {
				swiftBot.setUnderlight(decipherLED(s.getColor()), decipherColor(s.getColor()));
				AudioDevice.play(decipherNote(s.getColor()), false);
				Thread.sleep(1000);
				
				/* This line is MANDATORY or else the Java sound library will unreasonably break
				 *  and crash the entire program. Consider this a safeguard measure to keep things
				 *  in place.
				 */
				AudioDevice.stop();
				swiftBot.disableUnderlights();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* Deciphers the LED value of the Step object and returns the equivalent value mapped to the
	 * SwiftBot API.
	 */
	private static Underlight decipherLED(String color) {
		Underlight decipheredLED = Underlight.FRONT_LEFT;
		
		switch (color) {
		case "R":
			decipheredLED = Underlight.FRONT_LEFT;
			return decipheredLED;
		case "G":
			decipheredLED = Underlight.FRONT_RIGHT;
			return decipheredLED;
		case "B":
			decipheredLED = Underlight.BACK_LEFT;
			return decipheredLED;
		case "Y":
			decipheredLED = Underlight.BACK_RIGHT;
			return decipheredLED;
		default:
			decipheredLED = Underlight.FRONT_LEFT;
			break;
		}
		return decipheredLED;
	}
	
	/* Deciphers the Color value of the Step object and returns the equivalent RGB value accepted by the
	 * SwiftBot API.
	 */
	private static int[] decipherColor(String color) {
		int[] rgbValue = new int[2];
		
		switch (color) {
		case "R":
			rgbValue = CE_RED;
			return rgbValue;
		case "G":
			rgbValue = CE_GREEN;
			return rgbValue;
		case "B":
			rgbValue = CE_BLUE;
			return rgbValue;
		case "Y":
			rgbValue = CE_YELLOW;
			return rgbValue;
		}
		
		return rgbValue;
	}
	
	/* Deciphers the Color value of the Step object and returns the appropriate note to be played for the
	 * Step object in question.
	 */
	private static String decipherNote(String color) {
		String note = "";
		
		switch (color) {
		case "R":
			note = "Note_R.wav";
			return note;
		case "G":
			note = "Note_G.wav";
			return note;
		case "B":
			note = "Note_B.wav";
			return note;
		case "Y":
			note = "Note_Y.wav";
			return note;
		}
		
		return note;
	}
	
	/* As the name implies, this method instructs the SwiftBot to perform a "spin". 
	 * Although this may not be a complete 360 degree spin as per the assignment specification as per the
	 * assignment specification, it had to be done as external speakers or an HDMI cable is recommended for
	 * audio support. If the SwiftBot were to spin 360 degrees, it would wind up entangled in cables.
	 * As a result, it spins partly towards the left and right, then returns to its original position.
	 */
	private static void victoryDance(SwiftBotAPI swiftBot) 
			throws InterruptedException {
		
		swiftBot.move(35, 0, 1000);
		swiftBot.move(-35, 0, 1000);
		swiftBot.move(0, 35, 1000);
		swiftBot.move(0, -35, 1000);
		
		swiftBot.stopMove();
	}
	
	/*** -- PLAYER INPUT & VALIDATION SECTION -- ***/
	
	/* Core method to evaluate player input: comprises mainly of event listeners for each button and a number
	 * of validation checks in place to allocate points and decide whether the user's input sequence was
	 * right or not.
	 * 
	 * DISCLAIMER: This method is problematic and a number of errors are thrown if the user goes too quick
	 * with button presses and sometimes registers input twice. This is a hardware issue with the SwiftBot
	 * itself and cannot be resolved at this time. Additionally, the Java audio channel may not be free when
	 * the user attempts to press buttons quickly, which could wind up crashing the program.
	 * 
	 *  Perhaps, there lies a more efficient way to resolving input errors, but I tried the best I could with
	 *  roughly 5 weeks worth of Java experience. I might attempt to resolve these issues in the future.
	 */
	private static boolean awaitPlayerInput(SwiftBotAPI swiftBot) throws InterruptedException {
		
		long allowedTime = System.currentTimeMillis() + 7_000;
		
		/* A major problem with the way in which CE is implemented is the inability to process quick button
		 * presses. This forces the player to press buttons slowly so as to not break the game. In lieu of this,
		 * CE allocates more time to the player when the number of steps go beyond 4. Therefore, each even 
		 * number of steps in stepArray increases allowedTime value by roughly 2.5 seconds.
		 * 
		 * The following check does just that by keeping track of the magnitude to be multiplied by, and applies
		 * it to allowedTime.
		 */
		if (stepArray.size() > 4) {
			if ((stepArray.size() % 2) == 0) {
				allowedTime += (2_500 * timeMagnitude);
				timeMagnitude += 1;
			}
		}
		
		continueAllowed = true;
		allStepsCorrect = false;
		
		for (GpioPinDigitalInput button : swiftBot.BUTTONS) {
			button.addListener(new GpioPinListenerDigital() {
				public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
					if (event.getState().isLow()) {
						/* An attempt to throttle the IndexOutOfBoundException that pops up every once in a while
						 * when the user presses buttons two quick or the SwiftBot hardware registers a single input
						 * twice.
						 */ 
						if (currentStepIndex > stepArray.size() - 1) {
							return;
						}
						
						if (currentStepIndex <= (stepArray.size() - 1) && continueAllowed) {
							Step currentStep = stepArray.get(currentStepIndex);
							
							try {
								/* Set the underlight color for the corresponding underlight and play the
								 * corresponding note for the current step color.
								 */
								swiftBot.setUnderlight(decipherLED(colorEquivalent(event.getPin().getName())), decipherColor(colorEquivalent(event.getPin().getName())));
								Thread.sleep(100);
								AudioDevice.play(decipherNote(colorEquivalent(event.getPin().getName())), false);
								Thread.sleep(450);
								AudioDevice.stop();
								swiftBot.disableUnderlights();
								
							/* A series of console messages to tell the user that they'regoing too fast. 
							 * Not that it helps much as the program encounters an exception..
							 */
							} catch (UnsupportedAudioFileException e) {
								System.out.println("Hold on there, you're going too fast!");
							}
							catch (IOException e) {
								e.printStackTrace();
							} catch (LineUnavailableException e) {
								System.out.println("Hold on there, you're going too fast!");
								
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							boolean recordedOutcome = validatePress(event.getPin().getName(), stepArray.get(currentStepIndex));
							
							if (recordedOutcome) {
								// Decide terminal color based on score point value.
								scoreTerminalDisplay(stepArray.get(currentStepIndex).getPoints());
								
								// Allocate the points temporarily to playerScorePoints for addition to playerScore later.
								playerScorePoints += currentStep.getPoints();
								currentStepIndex += 1;
								
								if (currentStepIndex > stepArray.size() - 1) {
									allStepsCorrect = true;
									continueAllowed = false;
								}
							}
							else {
								// If the user guesses wrong, then prevent them from going any further by freezing
								// input until the round is over.
								continueAllowed = false;
								System.out.println(Simon.ANSI_RED + "Wrong color!\n" + Simon.ANSI_RESET);
								currentStepIndex += 1;
								
								continueAllowed = false;
							}
						}
					}
				}
			});
		}
		
		// Wait for current round to end.
		while ((System.currentTimeMillis() < allowedTime)) {
			
		}
		
		swiftBot.BUTTON_A.removeAllListeners();
		swiftBot.BUTTON_B.removeAllListeners();
		swiftBot.BUTTON_X.removeAllListeners();
		swiftBot.BUTTON_Y.removeAllListeners();
		
		return allStepsCorrect;
	}
	
	// This method colorfully displays points that the user has earned by guessing the current step correctly.
	private static void scoreTerminalDisplay(int point) {
		switch (point) {
		case 20:
			System.out.println(Simon.ANSI_YELLOW + "+20 points\n" + Simon.ANSI_RESET);
			break;
		case 50:
			System.out.println(Simon.ANSI_PURPLE + "+50 points\n" + Simon.ANSI_RESET);
			break;
		case 100:
			System.out.println(Simon.ANSI_BLUE + "+100 points\n" + Simon.ANSI_RESET);
			break;
		}
	}
	
	/* As the name implies, get the current buttonName and it's corresponding color and compare it with
	 * the color of currentStep. The utility method colorEquivalent() is used for converting the values
	 * to make the comparison.
	 */
	private static boolean validatePress(String buttonName, Step currentStep) {
		boolean outcome = false;
		
		if (colorEquivalent(buttonName).compareTo(currentStep.getColor()) == 0) {
			outcome = true;
			return outcome;
		}
		
		return outcome;
	}
	
	/* Classic Engine has a fixed assignment of button to colors as follows -
	 * Button A : FRONT_LEFT -> RED
	 * Button X: FRONT_RIGHT -> GREEN
	 * Button B: BACK_LEFT -> BLUE
	 * Button Y: BACK_RIGHT -> YELLOW
	 * 
	 * This method returns the corresponding color for the button parameter
	 */
	private static String colorEquivalent(String buttonName) {
		String color = "";
		
		switch (buttonName) {
		case "Button A":
			color = "R";
			return color;
		case "Button X":
			color = "G";
			return color;
		case "Button B":
			color = "B";
			return color;
		case "Button Y":
			color = "Y";
			return color;
		}
		
		return color;
	}
	
	/* Once the game has ended, check to see if the player has set a high score. If they have, a different
	 * sound effect is played than the usual GAME OVER sound effect from Konami's Snatcher.
	 * 
	 * It's worth noting that the sessionHighScore value is set to 0 by default, therefore the first time the
	 * player sets a high score for a session, the game doesn't congratulate them because it doesn't really
	 * seem like a "high score" per se.
	 * The next time they beat their previous score, however, they are greeted with a NEW HIGH SCORE prompt.
	 * This might seem slightly bizarre, but there's a certain method to my madness. 
	 */
	private static void displayPostGameResults() 
			throws UnsupportedAudioFileException, IOException, LineUnavailableException {
		
		if (!playedOnce) {
			AudioDevice.play("game_over.wav", false);
			playedOnce = true;
			sessionHighScore = playerScore;
		}
		else if (playedOnce && (playerScore > sessionHighScore)) {
			sessionHighScore = playerScore;
			AudioDevice.play("high_score.wav", false);
			System.out.println(Simon.ITALICS_START + "NEW HIGH SCORE!\n" + Simon.ITALICS_END);
		}
		else {
			AudioDevice.play("game_over.wav", false);
		}
		
		System.out.println("YOUR SCORE: " + playerScore);
	}
}
