package llm;

import java.util.ArrayList;
import java.util.List;

public class GameRecord {
    private boolean isSimpleMode;
    private int boardSize;
    private String bluePlayerType;
    private String redPlayerType;
    private List<Move> moves;
    private int finalBlueScore;
    private int finalRedScore;
    private String gameStatus;

    public GameRecord(boolean isSimpleMode, int boardSize, String bluePlayerType, String redPlayerType) {
        this.isSimpleMode = isSimpleMode;
        this.boardSize = boardSize;
        this.bluePlayerType = bluePlayerType;
        this.redPlayerType = redPlayerType;
        this.moves = new ArrayList<>();
        this.gameStatus = "In Progress";
    }

    // Adds a new move to the game record.
    public void addMove(int row, int col, char letter, String player) {
        moves.add(new Move(row, col, letter, player));
    }

    // 
    /**
     * Serializes the game record into a string format for storage.
     * First line contains game configuration
     * Subsequent lines contain individual moves
     * Reference from https://www.scaler.com/topics/java/stringbuilder-in-java/
     */
    public String serialize() {
        StringBuilder dataBuilder = new StringBuilder();
   
        dataBuilder.append(String.format("%b,%d,%s,%s,%d,%d,%s\n", 
            isSimpleMode, // boolean: true for simple mode, false for general mode
            boardSize,
            bluePlayerType,
            redPlayerType,
            finalBlueScore,
            finalRedScore,
            gameStatus)); // String: "In Progress", "Completed", or "Interrupted"
        
        for (Move move : moves) {
        	dataBuilder.append(move.toString()).append("\n"); // Add each move on a new line
        }
        return dataBuilder.toString();
    }

    // Deserialize a string format back into a GameRecord object.
    public static GameRecord deserialize(String data) {
    	// Split the file into lines
    	String[] lines = data.split("\n");

        // Takes the first line and splits it at each comma
        String[] header = lines[0].split(",");
       
        // Convert header values to appropriate types 
        boolean isSimpleMode = Boolean.parseBoolean(header[0]);
        int boardSize = Integer.parseInt(header[1]);
        String bluePlayerType = header[2];
        String redPlayerType = header[3];
        int blueScore = Integer.parseInt(header[4]);
        int redScore = Integer.parseInt(header[5]);
        String gameStatus = header[6];

        // Create record and set values
        GameRecord record = new GameRecord(isSimpleMode, boardSize, bluePlayerType, redPlayerType);
        record.setFinalScores(blueScore, redScore);
        record.setGameStatus(gameStatus);

        // Parse moves
        for (int i = 1; i < lines.length; i++) {
            if (!lines[i].trim().isEmpty()) {
                try {
                    Move move = Move.fromString(lines[i]);
                    record.moves.add(move);
                } catch (Exception e) {
                    System.err.println("Warning: Skipping invalid move at line " + i + ": " + lines[i]);
                }
            }
        }

        return record;            
    }

    // Contains information about the position, letter played, and player who made the move.
    public static class Move {
        private int row;
        private int col;
        private char letter;
        private String player;

        // Constructs a new Move object.
        public Move(int row, int col, char letter, String player) {
            this.row = row;
            this.col = col;
            this.letter = letter;
            this.player = player;
        }

        /**
         * Converts the move to a string representation for storage.
         * Format: "player,row,col,letter"
         */
        @Override
        public String toString() {
            return String.format("%s,%d,%d,%c", player, row, col, letter);
        }

        // Creates a Move object from its string representation.
        public static Move fromString(String str) {
            String[] parts = str.split(",");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid move format: " + str);
            }
            return new Move(		
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                parts[3].charAt(0),
                parts[0]
            );
        }

        // Getters
        public int getRow() { return row; }
        public int getCol() { return col; }
        public char getLetter() { return letter; }
        public String getPlayer() { return player; }
    }

    // Getters
    public boolean isSimpleMode() { return isSimpleMode; }
    public int getBoardSize() { return boardSize; }
    public String getBluePlayerType() { return bluePlayerType; }
    public String getRedPlayerType() { return redPlayerType; }
    public List<Move> getMoves() { return moves; }
    public int getFinalBlueScore() { return finalBlueScore; }
    public int getFinalRedScore() { return finalRedScore; }
    public String getGameStatus() { return gameStatus; }

    // Setters
    public void setFinalScores(int blueScore, int redScore) {
        this.finalBlueScore = blueScore;
        this.finalRedScore = redScore;
    }

    public void setGameStatus(String status) {
        this.gameStatus = status;
    }
}