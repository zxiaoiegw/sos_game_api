package llm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ComputerPlayer {
    private Random random;
    protected char playerSymbol; // 'B' for Blue, 'R' for Red    
	private GameMode gameMode;
    protected boolean isSimpleMode;

    // Make Move class public and static
    public static class Move {
        public int row;
        public int col;
        public char letter;
        public int score;

        public Move(int row, int col, char letter, int score) {
            this.row = row;
            this.col = col;
            this.letter = letter;
            this.score = score;
        }
    }

    public ComputerPlayer(char playerSymbol, GameMode gameMode, boolean isSimpleMode) {
        this.random = new Random();
        this.playerSymbol = playerSymbol;
        this.gameMode = gameMode;
        this.isSimpleMode = isSimpleMode;
    }

    // Make a move based on the current board state
    public Move makeMove(char[][] board) {
        List<Move> possibleMoves = new ArrayList<>();
        
        // Check all possible moves
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == ' ') {
                    // Try 'S'
                    int sScore = evaluateMove(board, i, j, 'S');
                    possibleMoves.add(new Move(i, j, 'S', sScore));
                    
                    // Try 'O'
                    int oScore = evaluateMove(board, i, j, 'O');
                    possibleMoves.add(new Move(i, j, 'O', oScore));
                }
            }
        }

        // Choose the best move
        return getBestMove(possibleMoves);
    }

    // Evaluate a potential move's score
    private int evaluateMove(char[][] board, int row, int col, char letter) {
        int score = 0;
        
        // Temporary place the letter
        char original = board[row][col];
        board[row][col] = letter;
        
        // Check all directions for potential SOS
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            int dr = dir[0];
            int dc = dir[1];
            
            // If this move completes an SOS
            if (checkSOS(board, row, col, dr, dc)) {
                score += 100; // High score for completing SOS
            }
            
            // If this move sets up a future SOS
            if (checkPotentialSOS(board, row, col, letter)) {
                score += 50; // Medium score for setting up SOS
            }
            
            // If this move blocks opponent's SOS
            if (checkBlockingMove(board, row, col, letter)) {
                score += 75; // High score for blocking opponent
            }
        }
        
        // Restore the board
        board[row][col] = original;
        
        return score;
    }

    // Check if there's a complete SOS
    private boolean checkSOS(char[][] board, int row, int col, int dr, int dc) {
        char current = board[row][col];
        int n = board.length;
        
        // Check if current position is 'S' and part of SOS
        if (current == 'S') {
            // Check forward
            if (isValidPos(row + dr, col + dc, n) && 
                isValidPos(row + 2 * dr, col + 2 * dc, n)) {
                if (board[row + dr][col + dc] == 'O' && 
                    board[row + 2 * dr][col + 2 * dc] == 'S') {
                    return true;
                }
            }
            
            // Check backward
            if (isValidPos(row - dr, col - dc, n) && 
                isValidPos(row - 2 * dr, col - 2 * dc, n)) {
                if (board[row - dr][col - dc] == 'O' && 
                    board[row - 2 * dr][col - 2 * dc] == 'S') {
                    return true;
                }
            }
        }
        
        // Check if current position is 'O' and part of SOS
        if (current == 'O') {
            if (isValidPos(row - dr, col - dc, n) && 
                isValidPos(row + dr, col + dc, n)) {
                if (board[row - dr][col - dc] == 'S' && 
                    board[row + dr][col + dc] == 'S') {
                    return true;
                }
            }
        }
        
        return false;
    }

    // Check if this move creates potential for future SOS
    private boolean checkPotentialSOS(char[][] board, int row, int col, char letter) {
        int n = board.length;
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            int dr = dir[0];
            int dc = dir[1];
            
            if (letter == 'S') {
                // Check if we can create S_S pattern
                if (isValidPos(row + 2 * dr, col + 2 * dc, n) &&
                    board[row + 2 * dr][col + 2 * dc] == 'S' &&
                    board[row + dr][col + dc] == ' ') {
                    return true;
                }
            } else if (letter == 'O') {
                // Check if we can complete S_S pattern
                if (isValidPos(row - dr, col - dc, n) &&
                    isValidPos(row + dr, col + dc, n) &&
                    board[row - dr][col - dc] == 'S' &&
                    board[row + dr][col + dc] == ' ') {
                    return true;
                }
            }
        }
        return false;
    }

    // Check if this move blocks opponent's potential SOS
    private boolean checkBlockingMove(char[][] board, int row, int col, char letter) {
        // Similar to checkPotentialSOS but checks opponent's possibilities
        return checkPotentialSOS(board, row, col, letter);
    }

    // check if position is valid
    private boolean isValidPos(int row, int col, int n) {
        return row >= 0 && row < n && col >= 0 && col < n;
    }

    // Choose the best move from possible moves
    private Move getBestMove(List<Move> moves) {
        if (moves.isEmpty()) {
            return null;
        }

        // For simple mode or when there's a winning move, choose the highest score
        Move bestMove = null;
        int maxScore = Integer.MIN_VALUE;
        
        for (Move move : moves) {
            if (move.score > maxScore) {
                maxScore = move.score;
                bestMove = move;
            }
        }
        
        // If no good moves found (all scores 0), make a random move
        if (maxScore == 0) {
            return moves.get(random.nextInt(moves.size()));
        }
        
        return bestMove;
    }
    
    public char getPlayerSymbol() {
		return playerSymbol;
	}
}