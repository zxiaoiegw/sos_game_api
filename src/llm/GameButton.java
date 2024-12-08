package llm;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GameButton extends JButton {
    private java.util.List<Line> lines = new ArrayList<>();

    public GameButton(String text) {
        super(text);
    }
    
    // Add a line to be drawn over the button
    public void addLine(Line line) {
        lines.add(line);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw lines over the button
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3)); // thickness of the line
        int width = getWidth();
        int height = getHeight();

        for (Line line : lines) {
            g2.setColor(line.color);
            switch (line.direction) {
                case HORIZONTAL:
                    g2.drawLine(0, height / 2, width, height / 2);
                    break;
                case VERTICAL:
                    g2.drawLine(width / 2, 0, width / 2, height);
                    break;
                case DIAGONAL_RIGHT:
                    g2.drawLine(0, 0, width, height);
                    break;
                case DIAGONAL_LEFT:
                    g2.drawLine(width, 0, 0, height);
                    break;
            }
        }
    }

    // Represent a line to be drawn
    public static class Line {
        public Color color; // Color of the line
        public Direction direction; // Direction of the line

        public Line(Color color, Direction direction) {
            this.color = color;
            this.direction = direction;
        }
    }

    // Enum to represent the directions for lines
    public enum Direction {
        HORIZONTAL,
        VERTICAL,
        DIAGONAL_RIGHT,
        DIAGONAL_LEFT
    }
}
