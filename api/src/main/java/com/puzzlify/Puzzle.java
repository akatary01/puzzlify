package com.puzzlify;

import java.awt.image.BufferedImage;

public class Puzzle {
    public record Pixel(int x, int y) {}
    public record Pair<T, U>(T first, U second) {}

    public record Cell(Pixel[] coordinates) {}
    public record Edge(Pixel start, Pixel end) {}
    
    public static Cell[][] constructGrid(final BufferedImage image, final int rows, final int cols) {
        final Cell[][] grid = new Cell[rows][cols];
        
        final int h = image.getHeight();
        final int w = image.getWidth();
        
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
                        // only add non-transparent pixels to the cell
                        if (image.getColorModel().hasAlpha() && ((image.getRGB(l + b*j, k + a*i) >> 24) & 0xff) == 255) {
                            coordinates[k*b + l] = new Pixel(l + b*j, k + a*i);
                        }
                            
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
