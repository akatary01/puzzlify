package com.puzzlify;

public class Puzzle {
    public record Pixel(int x, int y) {}
    public record Pair<T, U>(T first, U second) {}

    public record Cell(Pixel[] coordinates) {}
    public record Edge(Pixel start, Pixel end) {}
    
    public static Cell[][] constructGrid(final int w, final int h, final int rows, final int cols) {
        final Cell[][] grid = new Cell[rows][cols];
        
        final int a = (int) Math.floor(h/rows);
        final int b = (int) Math.floor(w/cols);
        for (int i = 0; i < rows; i++) {
            final Cell[] row = new Cell[cols];
            // the remaining bottom pixels
            final int rH = i == rows - 1 ? h % rows : 0;

            for (int j = 0; j < cols; j++) {
                // the remaining right-most pixels
                final int rC = j == cols - 1 ? w % cols : 0;
                final Pixel[] coordinates = new Pixel[(a + rH)*(b + rC)];
                for (int k = 0; k < a + rH; k++) {
                    for (int l = 0; l < b + rC; l++) {
                        coordinates[k*b + l] = new Pixel(k + a*i, l + b*j);
                    }
                }

                row[j] = new Cell(coordinates);
            }
            grid[i] = row;
        }
        return grid;
    }

    public static Pair<Cell, Cell> cut(Edge edge, Cell cellA, Cell cellB) {
        throw new UnsupportedOperationException("Not implemented yet");
        // TODO: Ahmed
    }
    private Puzzle() {}
}
