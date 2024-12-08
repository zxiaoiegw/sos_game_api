package llm;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameRecordService {
    private static final String RECORDS_DIR = "game_records"; // Directory where game records are stored
    private GameRecord currentGame; // Currently recording game
    private String currentFileName; // Filename for current game record
    private boolean isRecording = false;

    // Initializes the GameRecordService by creating the records directory 
    public GameRecordService() {
        try {
            Files.createDirectories(Paths.get(RECORDS_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Starts recording a new game with the specified configuration.
    public void startRecording(boolean isSimpleMode, int boardSize, String bluePlayerType, String redPlayerType) {       
    	// Creates a new GameRecord object to store game information
    	currentGame = new GameRecord(isSimpleMode, boardSize, bluePlayerType, redPlayerType);
        
    	// Generate filename for this game
    	currentFileName = generateFileName(isSimpleMode, bluePlayerType, redPlayerType);
        isRecording = true;
        saveGame();       
    }

    // Records a single move in the current game.
    public void recordMove(int row, int col, char letter, String player) {
    	// Check recording status
    	if (currentGame != null && isRecording) {
            currentGame.addMove(row, col, letter, player);
            saveGame();
        }
    }

    // handle interrupted games
    public void interruptRecording(int blueScore, int redScore) {
        if (currentGame != null && isRecording) {
            currentGame.setFinalScores(blueScore, redScore);
            currentGame.setGameStatus("Interrupted");
            saveGame();
            isRecording = false;
            currentGame = null;
        }
    }

    // Ends the game recording with final scores
    public void endRecording(int blueScore, int redScore) {
        if (currentGame != null && isRecording) {
            currentGame.setFinalScores(blueScore, redScore);
            currentGame.setGameStatus("Completed");
            saveGame();
            isRecording = false;
            currentGame = null;
        }
    }

    /**
     * Saves the current game state to file.
     * Uses the GameRecord's serialize method to convert game data to string format.
     */
    private void saveGame() {
        if (currentGame == null) return;
        
        try {
            Path filePath = Paths.get(RECORDS_DIR, currentFileName);
            String content = currentGame.serialize();
            Files.write(filePath, content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads a game record from file.
    public GameRecord loadGame(String fileName) {
        try {
            Path filePath = Paths.get(RECORDS_DIR, fileName);
            String content = new String(Files.readAllBytes(filePath));
            return GameRecord.deserialize(content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } 
    }

    // Lists all recorded games in the records directory.
    public String[] listRecordedGames() {
        try {
        	return Files.list(Paths.get(RECORDS_DIR))     // Lists all files in game_records folder
                    .map(Path::getFileName)                    // Gets the filename
                    .map(Path::toString)                       // Converts to String
                    .filter(name -> name.endsWith(".txt"))     // Only gets .txt files
                    .toArray(String[]::new);                   // Converts to String array
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0]; // Returns empty array if error
        }
    }

    /**
     * Reference from https://alexmanrique.com/blog/development/2020/10/31/creating-a-jackson-serializer-in-java.html
     * Generates a filename for a game record based on its configuration
     * and current timestamp.
     */
    private String generateFileName(boolean isSimpleMode, String blueType, String redType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String gameType = isSimpleMode ? "simple" : "general";
        return String.format("%s_%s_vs_%s_%s.txt", gameType, blueType, redType, timestamp);
    }
}