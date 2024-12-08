package llm;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;

public class ComputerPlayerAPI extends ComputerPlayer {
    private final HttpClient client;
    private final String apiKey;
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String SYSTEM_PROMPT = """
        You are playing an SOS game. Make a move by analyzing the board.
	    Rules:
	    1. Place 'S' or 'O' on empty cells
	    2. Goal is to form SOS sequences
	    3. Simple mode (3x3): First SOS formations wins
	    4. General mode (4x4 to 8x8): Most SOS formations wins
	    
	    Respond only with: row,column,letter
	    Example: 2,1,S
    """;

    public ComputerPlayerAPI(char playerSymbol, GameMode gameMode, boolean isSimpleMode, String apiKey) {
        super(playerSymbol, gameMode, isSimpleMode);
        this.client = HttpClient.newHttpClient();
        this.apiKey = apiKey;
    }

    @Override
    public Move makeMove(char[][] board) {
        try {
            String boardState = createBoardStateMessage(board);
            
            // Create request body
            JSONObject requestBody = new JSONObject()
                .put("model", "claude-3-sonnet-20240229")
                .put("max_tokens", 1000) // Increased to accommodate detailed reasoning
                .put("system", SYSTEM_PROMPT)
                .put("messages", new JSONObject[]{ 
                    new JSONObject()
                        .put("role", "user")
                        .put("content", boardState)
                });

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

            // Send request and get response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JSONObject jsonResponse = new JSONObject(response.body());
                String content = jsonResponse.getJSONArray("content").getJSONObject(0).getString("text");
                return validateMoveResponse(content, board);
            } else {
                System.err.println("API Error: " + response.statusCode() + " - " + response.body());
                return super.makeMove(board);
            }
            
        } catch (Exception e) {
            System.err.println("Claude API error: " + e.getMessage());
            return super.makeMove(board);
        }
    }

    private String createBoardStateMessage(char[][] board) {
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("Current board state (").append(board.length).append("x").append(board.length).append("):\n");
        
        // Add board visualization
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                dataBuilder.append(board[i][j] == ' ' ? '.' : board[i][j]);
                if (j < board.length - 1) dataBuilder.append('|');
            }
            dataBuilder.append('\n');
            if (i < board.length - 1) {
                dataBuilder.append("-".repeat(board.length * 2 - 1)).append('\n');
            }
        }
        
        // Add game context
        dataBuilder.append("\nGame mode: ").append(isSimpleMode ? "Simple (First SOS wins)" : "General (Most SOS wins)");
        String playerColor = (playerSymbol == 'B') ? "Blue" : "Red";
        dataBuilder.append("\nYou are playing as: ").append(playerColor);
        dataBuilder.append("\nAnalyze the board and determine the best strategic move.\n");
        dataBuilder.append("Show your reasoning using the specified tags (<thinking>, <step>, <reflection>, <reward>)");
        
        return dataBuilder.toString();
    }

    private Move validateMoveResponse(String response, char[][] board) {
        try {
            // Extract just the final move coordinates
            String[] parts = response.lines()
                .filter(line -> line.matches("\\d+,\\d+,[SO]"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No valid move found in response"))
                .split(",");

            int row = Integer.parseInt(parts[0].trim());
            int col = Integer.parseInt(parts[1].trim());
            char letter = parts[2].trim().charAt(0);

            if (isValidMove(row, col, letter, board)) {
                // Log the AI's reasoning for debugging/analysis
                System.out.println("AI Reasoning:\n" + response);
                return new Move(row, col, letter, 100);
            }
        } catch (Exception e) {
            System.err.println("Error parsing Claude response: " + e.getMessage());
        }
        
        return super.makeMove(board);
    }

    private boolean isValidMove(int row, int col, char letter, char[][] board) {
        return row >= 0 && row < board.length &&
               col >= 0 && col < board.length &&
               board[row][col] == ' ' &&
               (letter == 'S' || letter == 'O');
    }

    public void cleanup() {
        
    }
}