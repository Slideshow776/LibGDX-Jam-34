package no.sandramoen.libgdx34.utils;

import com.badlogic.gdx.utils.Array;

import java.util.Locale;
import java.util.Random;

public class GameBoard {
    public Array<Array<Cell>> rows;
    public int playerRow, playerCol;

    private final boolean IS_PRINT = true;
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Random random = new Random();


    public GameBoard() {
        rows = new Array<>();
        generateGrid();

        if (IS_PRINT) {
            printBoard();
        }
    }


    private void generateGrid() {
        int[] rowLengths = {3, 4, 5, 4, 3}; // Example hexagon shape
        String uniqueLetters = shuffledAlphabet();

        int index = 0;
        for (int r = 0; r < rowLengths.length; r++) {
            Array<Cell> row = new Array<>();
            for (int c = 0; c < rowLengths[r]; c++) {
                Cell cell = new Cell();
                cell.letter = String.valueOf(uniqueLetters.charAt(index++));
                row.add(cell);
            }
            rows.add(row);
        }

        // Place player at start
        playerRow = 0;
        playerCol = 0;
        rows.get(playerRow).get(playerCol).is_player_here = true;

        // Place goal at bottom-right
        int lastRow = rows.size - 1;
        int lastCol = rows.get(lastRow).size - 1;
        rows.get(lastRow).get(lastCol).is_goal_here = true;
    }


    private String shuffledAlphabet() {
        char[] chars = LETTERS.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }


    public void movePlayerIfMatch(char typedLetter) {
        for (int[] n : getNeighbors(playerRow, playerCol)) {
            Cell neighbor = rows.get(n[0]).get(n[1]);
            if (neighbor.letter.equalsIgnoreCase(String.valueOf(typedLetter))) {
                // Move player
                rows.get(playerRow).get(playerCol).is_player_here = false;
                playerRow = n[0];
                playerCol = n[1];
                neighbor.is_player_here = true;

                if (IS_PRINT) {
                    printBoard();
                    printNeighborsOfLetter(typedLetter);
                }
                return;
            }
        }
    }


    public Array<int[]> getNeighbors(int row, int col) {
        Array<int[]> neighbors = new Array<>();

        // Directions: up-left, up-right, left, right, down-left, down-right
        int[][] directions = {
            {-1, 0}, {-1, 1},  // upper neighbors
            {0, -1}, {0, 1},   // same row neighbors
            {1, 0}, {1, 1}     // lower neighbors
        };

        int currentRowSize = rows.get(row).size;

        for (int[] d : directions) {
            int nRow = row + d[0];
            if (nRow < 0 || nRow >= rows.size) continue;

            int neighborRowSize = rows.get(nRow).size;
            // Column offset due to jagged row layout
            int colOffset = (neighborRowSize - currentRowSize - 1) / 2;
            int nCol = col + d[1] + colOffset;

            if (nCol < 0 || nCol >= neighborRowSize) continue;

            neighbors.add(new int[]{nRow, nCol});
        }

        return neighbors;
    }


    public void printNeighborsOfLetter(char letter) {
        for (int r = 0; r < rows.size; r++) {
            for (int c = 0; c < rows.get(r).size; c++) {
                Cell cell = rows.get(r).get(c);
                if (cell.letter.equalsIgnoreCase(String.valueOf(letter))) {
                    Array<int[]> neighbors = getNeighbors(r, c);
                    System.out.print(letter + " has " + neighbors.size + " neighbors: ");
                    for (int[] n : neighbors) {
                        System.out.print(rows.get(n[0]).get(n[1]).letter + " ");
                    }
                    System.out.println();
                    return;
                }
            }
        }
    }


    public void printBoard() {
        System.out.println("=== GAME BOARD ===");

        int maxLen = rows.get(2).size; // middle row is widest

        for (int r = 0; r < rows.size; r++) {
            Array<Cell> row = rows.get(r);

            // Build row like "A-B-C"
            StringBuilder rowBuilder = new StringBuilder();
            for (int c = 0; c < row.size; c++) {
                Cell cell = row.get(c);
                String symbol = cell.letter;

                if (cell.is_player_here) symbol = symbol.toLowerCase(); // player
                else if (cell.is_goal_here) symbol = symbol.toLowerCase(); // goal

                rowBuilder.append(symbol);
                if (c < row.size - 1) rowBuilder.append("-");
            }

            // Left/right padding with dashes
            int missing = maxLen - row.size;
            String pad = "";
            for (int i = 0; i < missing; i++) pad += "-";

            System.out.println(pad + rowBuilder + pad);
        }

        System.out.println("==================");
    }
}
