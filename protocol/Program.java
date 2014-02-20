package protocol;

import java.io.*;

import client.*;

/**
 * Entry point of the program. Starts the client and links the used MAC
 * protocol.
 * 
 * @author Jaco ter Braak, Twente University
 * @version 05-12-2013
 */
public class Program {

	// Change to your group number (use a student number)
	private static int groupId = 1460501;

	// Change to your group password (doesn't matter what it is,
	// as long as everyone in the group uses the same string)
	private static String password = "stroopwafel";

	// Change to your protocol implementation
	private static IMACProtocol protocol = new UberProtocol();

	// Challenge server address
	private static String serverAddress = "130.89.235.123";

	// Challenge server port
	private static int serverPort = 8001;

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * DO NOT EDIT BELOW THIS LINE
	 */
	public static void main(String[] args) {
		MACChallengeClient client = null;
		try {
			System.out.print("Starting client... ");
			
			// Create the client
			client = new MACChallengeClient(serverAddress, serverPort, groupId,
					password);

			System.out.println("Done.");
			
			// Set protocol
			client.setListener(protocol);

			System.out.println("Press Enter to start the simulation...");
			System.out
					.println("(Simulation will also be started automatically if another client in the group issues the start command)");

			InputStream inputStream = new BufferedInputStream(System.in);

			// Wait for either a key press
			// or for a signal from the server to start
			while (!client.isSimulationStarted()) {

				if (inputStream.available() > 0) {
					inputStream.read();
					client.requestStart();
					while (inputStream.available() > 0)
						inputStream.read();
				}
				Thread.sleep(10);
			}

			System.out.println("Simulation started!");

			// Wait until the simulation is finished
			while (!client.isSimulationFinished()) {
				Thread.sleep(10);
			}

			System.out
					.println("Simulation finished! Check your performance on the server web interface.");

		} catch (IOException e) {
			System.out.print("Could not start the client, because: ");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Operation interrupted.");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.print("Unexpected Exception: ");
			e.printStackTrace();
		} finally {
			if (client != null) {
				System.out.print("Shutting down client... ");
				client.stop();
				System.out.println("Done.");
			}
			System.out.println("Terminating program.");
		}
	}
}
