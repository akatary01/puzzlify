package com.puzzlify;

public class Puzzle {
    public record Point(int x, int y) {}
    public record Pair<T, U>(T first, U second) {}

    public record Cell(Point[] coordinates) {}
    public record Edge(Point start, Point end) {}
    
    public static Cell[][] constructGrid(int w, int h, int rows, int cols) {
        final Cell[][] grid = new Cell[rows][cols];
        
        final int a = (int) Math.floor(h/rows);
        final int b = (int) Math.floor(w/cols);
        for (int i = 0; i < rows; i++) {
            final Cell[] row = new Cell[cols];
            int rh = 0; 
            if (i == rows - 1) {
                rh = h % rows;
            }
            for (int j = 0; j < cols; j++) {
                int rc = 0;
                if (j == cols - 1) {
                    rc = w % cols;
                }
                // cell (i, j) contains pixels with 
                // x in [h//rows*i, h//rows*(i + 1))
                // y in [w//cols*j, w//cols*(j + 1))
                final Point[] coordinates = new Point[(a + rh)*(b + rc)];
                for (int k = 0; k < a + rh; k++) {
                    for (int l = 0; l < b + rc; l++) {
                        coordinates[k*b + l] = new Point(k + a*i, l + b*j);
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
