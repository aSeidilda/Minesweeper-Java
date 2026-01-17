import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class Minesweeper {
    static final char MINE = '*';
    static final char HIDDEN = '.';
    static final char EMPTY = ' ';
    static final char FLAG = 'F';

    static int SIZE = 12;
    static int MINES = 18;
    //Easy: 9: 10; 12: 18; 16: 32
    //Medium: 9: 15; 12: 27; 16: 47
    //Hard: 9: 20; 12: 36; 16: 62

    private static final int flags = MINES;
    private static int seconds = 0;

    static char[][] visibleBoard = new char[SIZE][SIZE];
    static char[][] realBoard = new char[SIZE][SIZE];
    static boolean[][] revealedBoard = new boolean[SIZE][SIZE];
    static boolean firstMove = true;
    static Map<Character, Color> colorMap = Map.of('1', Color.BLUE, '2', Color.GREEN, '3', Color.RED, '4', Color.MAGENTA,
            '5', Color.ORANGE, '6', Color.CYAN, '7', Color.YELLOW, '8', Color.BLACK);

    static JFrame frame;
    static JPanel topPanel = new JPanel();
    static JPanel centerPanel = new JPanel();
    static JLabel timeLabel;
    static JLabel flagsLabel;
    static JLabel result;
    static JButton[][] visibleButtonBoard = new JButton[SIZE][SIZE];

    static Timer timer;

    static class ButtonMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            JButton clicked = (JButton) e.getSource();
            int row = (int) clicked.getClientProperty("row");
            int column = (int) clicked.getClientProperty("column");

            if (visibleBoard[row][column] != EMPTY && visibleBoard[row][column] == MINE || visibleBoard[row][column] == HIDDEN || visibleBoard[row][column] == FLAG) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (firstMove) {
                        generateMines(row, column);
                        setRealBoard();
                        firstMove = false;
                    }

                    if (realBoard[row][column] == MINE) {
                        timer.stop();
                        for (int i = 0; i < SIZE; i++) {
                            for (int j = 0; j < SIZE; j++) {
                                if (realBoard[i][j] == MINE) {
                                    visibleBoard[i][j] = MINE;
                                    visibleButtonBoard[i][j].setText(String.valueOf(MINE));
                                }
                            }
                        }
                        printBoard();
                        System.out.println("Game Over!");
                        result = new JLabel("Game Over!");
                        System.out.println("Time: " + seconds);

                        centerPanel.removeAll();
                        centerPanel.revalidate();
                        centerPanel.repaint();
                        centerPanel.add(result);
                    } else {
                        reveal(row, column);
                        printBoard();
                        if (hasWon()) {
                            timer.stop();
                            printBoard();
                            System.out.println("You Win!");
                            result = new JLabel("You Win!");
                            System.out.println("Time: " + seconds);

                            centerPanel.removeAll();
                            centerPanel.revalidate();
                            centerPanel.repaint();
                            centerPanel.add(result);
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (visibleBoard[row][column] == HIDDEN) {
                        visibleBoard[row][column] = FLAG;
                        visibleButtonBoard[row][column].setText(String.valueOf(FLAG));
                        visibleButtonBoard[row][column].setBackground(Color.PINK);
                        flagsLabel.setText("Flags: " + --MINES);
                        printBoard();
                    } else if (visibleBoard[row][column] == FLAG) {
                        visibleBoard[row][column] = HIDDEN;
                        visibleButtonBoard[row][column].setText(String.valueOf(EMPTY));
                        visibleButtonBoard[row][column].setBackground(UIManager.getColor("Button.background"));
                        flagsLabel.setText("Flags: " + ++MINES);
                        printBoard();
                    }
                }
            }
        }
    }

    static void generateMines(int avoidRow, int avoidColumn) {
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(realBoard[i], EMPTY);
        }

        int placed = 0;
        Random random = new Random();
        while (placed < MINES) {
            int row = random.nextInt(SIZE);
            int column = random.nextInt(SIZE);

            if (realBoard[row][column] != MINE && (Math.abs(row - avoidRow) > 1 || Math.abs(column - avoidColumn) > 1)) {
                realBoard[row][column] = MINE;
                placed++;
            }
        }
    }

    static void setRealBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (realBoard[row][column] == MINE) {continue;}

                int count = 0;
                for (int drow = -1; drow <= 1; drow++) {
                    for (int dcolumn = -1; dcolumn <= 1; dcolumn++) {
                        int nearRow = row + drow, nearColumn = column + dcolumn;
                        if (inBounds(nearRow, nearColumn) && realBoard[nearRow][nearColumn] == MINE) {count++;}
                    }
                }
                if (count > 0) {
                    realBoard[row][column] = (char) ('0' + count);
                }
            }
        }
    }

    static boolean inBounds(int row, int column) {
        return row >= 0 && row < SIZE && column >= 0 && column < SIZE;
    }

    static void reveal(int row, int column) {
        if (!inBounds(row, column) || revealedBoard[row][column]) {return;}

        revealedBoard[row][column] = true;
        visibleBoard[row][column] = realBoard[row][column];
        visibleButtonBoard[row][column].setText(String.valueOf(realBoard[row][column]));
        visibleButtonBoard[row][column].setContentAreaFilled(false);
        visibleButtonBoard[row][column].setFocusPainted(false);
        visibleButtonBoard[row][column].setBorderPainted(false);

        visibleButtonBoard[row][column].setForeground(colorMap.get(visibleBoard[row][column]));

        if (realBoard[row][column] == EMPTY) {
            for (int drow = -1; drow <= 1; drow++) {
                for (int dcolumn = -1; dcolumn <= 1; dcolumn++) {
                    if (drow != 0 || dcolumn != 0) {
                        reveal(row + drow, column + dcolumn);
                    }
                }
            }
        }
    }

    static void printBoard() {
        for (int i = 0; i < SIZE; i++) {
            System.out.print(" -");
        }
        System.out.println();
        for (int row = 0; row < SIZE; row++) {
            System.out.print('|');
            for (int column = 0; column < SIZE; column++) {
                System.out.print(visibleBoard[row][column] + " ");
            }
            System.out.print('|');
            System.out.println();
        }
        for (int i = 0; i < SIZE; i++) {
            System.out.print(" -");
        }
        System.out.println();
    }

    static boolean hasWon() {
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (!revealedBoard[row][column] && realBoard[row][column] != MINE) {return false;}
            }
        }

        return true;
    }

    static void initializeButtons() {
        centerPanel.removeAll();
        for (int i = 0; i < SIZE; i++) {
            Arrays.fill(visibleBoard[i], HIDDEN);
            Arrays.fill(revealedBoard[i], false);
            for (int j = 0; j < SIZE; j++) {
                JButton button = new JButton();
                button.putClientProperty("row", i);
                button.putClientProperty("column", j);
                button.setText(String.valueOf(EMPTY));
                button.addMouseListener(new ButtonMouseListener());

                visibleButtonBoard[i][j] = button;
                centerPanel.add(button);
            }
        }
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    static void restart() {
        MINES = flags;
        seconds = 0;
        firstMove = true;
        timer.start();
        timeLabel.setText("Time: " + seconds);
        flagsLabel.setText("Flags: " + MINES);
        initializeButtons();
    }

    static void start() {
        frame = new JFrame("Minesweeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        timeLabel = new JLabel("Time: 0");
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                seconds++;
                timeLabel.setText("Time: " + seconds);
            }
        });
        timer.start();

        flagsLabel = new JLabel("Flags: " + MINES);
        JButton restart = new JButton("Restart");
        restart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                restart();
            }
        });

        topPanel.add(timeLabel);
        topPanel.add(restart);
        topPanel.add(flagsLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        centerPanel.setLayout(new GridLayout(SIZE, SIZE));
        initializeButtons();

        frame.add(centerPanel, BorderLayout.CENTER);
        if (SIZE == 16) {
            frame.setSize(700, 700);
        } else {
            frame.setSize(550, 550);
        }

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        start();
    }
}
