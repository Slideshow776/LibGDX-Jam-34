package no.sandramoen.libgdx34.utils;

import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class GameBoard {
    public Array<Array<Cell>> rows;
    public int playerRow, playerCol;

    private final boolean IS_PRINT = false;
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Random random = new Random();


    public GameBoard() {
        rows = new Array<>();
        generateGrid();
        placePlayerAndGoalRandomly();
        placeRandomKey();

        if (IS_PRINT) {
            printBoard();
        }
    }


    public boolean checkPlayerReachedGoalAndShuffle() {
        Cell playerCell = rows.get(playerRow).get(playerCol);

        if (playerCell.is_goal_here) {
            // Remove old goal
            playerCell.is_goal_here = false;

            // Shuffle letters
            shuffle();

            // Place new goal randomly
            placeRandomGoal();

            return true; // goal was reached
        }

        return false; // goal not reached
    }


    public boolean checkIfKey() {
        Cell playerCell = rows.get(playerRow).get(playerCol);
        if (playerCell.is_key_here) {
            playerCell.is_key_here = false;
            return true;
        }

        return false;
    }


    private void placePlayerAndGoalRandomly() {
        // 1️⃣ Clear previous player and goal
        for (Array<Cell> row : rows) {
            for (Cell cell : row) {
                cell.is_player_here = false;
                cell.is_goal_here = false;
            }
        }

        // 2️⃣ Pick a random cell for the player and assign to class fields
        this.playerRow = random.nextInt(rows.size);
        this.playerCol = random.nextInt(rows.get(this.playerRow).size);
        rows.get(this.playerRow).get(this.playerCol).is_player_here = true;

        // 3️⃣ Pick a random neighbor for the goal
        Array<int[]> neighbors = getNeighbors(this.playerRow, this.playerCol);
        if (neighbors.size > 0) {
            int[] goalPos = neighbors.get(random.nextInt(neighbors.size));
            rows.get(goalPos[0]).get(goalPos[1]).is_goal_here = true;
        } else {
            // fallback
            rows.get(this.playerRow).get(this.playerCol).is_goal_here = true;
        }
    }


    public void placeRandomKey() {
        Array<int[]> emptyCells = new Array<>();

        for (int r = 0; r < rows.size; r++) {
            for (int c = 0; c < rows.get(r).size; c++) {
                Cell cell = rows.get(r).get(c);
                cell.is_key_here = false; // clear old key
                if (!cell.is_player_here && !cell.is_goal_here) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        if (emptyCells.size > 0) {
            int[] keyPos = emptyCells.get(random.nextInt(emptyCells.size));
            rows.get(keyPos[0]).get(keyPos[1]).is_key_here = true;
        }
    }


    private void placeRandomGoal() {
        Array<int[]> emptyCells = new Array<>();

        // Collect all cells that are not the player
        for (int r = 0; r < rows.size; r++) {
            for (int c = 0; c < rows.get(r).size; c++) {
                Cell cell = rows.get(r).get(c);
                // TEtt: I think the goals weren't always getting cleared before.
                cell.is_goal_here = false;
                if (!cell.is_player_here) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }

        // Pick one at random
        int[] goalPos = emptyCells.get(random.nextInt(emptyCells.size));
        rows.get(goalPos[0]).get(goalPos[1]).is_goal_here = true;
    }


    public void shuffle() {
        Array<String> letters = new Array<>();

        // Collect letters from all cells
        for (Array<Cell> row : rows) {
            for (Cell cell : row) {
                letters.add(cell.letter);
            }
        }

        // Shuffle letters
        for (int i = letters.size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String tmp = letters.get(i);
            letters.set(i, letters.get(j));
            letters.set(j, tmp);
        }

        // Reassign letters to cells
        int index = 0;
        for (Array<Cell> row : rows) {
            for (Cell cell : row) {
                cell.letter = letters.get(index++);
            }
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


    public boolean movePlayerIfMatch(char typedLetter, boolean hasKey) {
        for (int[] n : getNeighbors(playerRow, playerCol)) {
            Cell neighbor = rows.get(n[0]).get(n[1]);
            if (neighbor.letter.equalsIgnoreCase(String.valueOf(typedLetter))) {

                // 🚪 Prevent moving onto goal if it's locked
                if (neighbor.is_goal_here && !hasKey) {
                    return false; // block movement until key is collected
                }

                // Move player
                rows.get(playerRow).get(playerCol).is_player_here = false;
                playerRow = n[0];
                playerCol = n[1];
                neighbor.is_player_here = true;

                if (IS_PRINT) {
                    printBoard();
                    printNeighborsOfLetter(typedLetter);
                }
                return true;
            }
        }
        return false;
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
                else if (cell.is_key_here) symbol = symbol.toLowerCase(); // goal

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
