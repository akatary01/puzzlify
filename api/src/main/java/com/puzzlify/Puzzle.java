package com.puzzlify;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Optional;

public class Puzzle {
    public record Pixel(int x, int y) {
        Pixel translate(int deltaX, int deltaY) {
            return new Pixel(x() + deltaX, y() + deltaY);
        }
        boolean between(Pixel upper, Pixel lower) {
            return lower.x() <= x() && x() <= upper.x() && lower.y() <= y() && y() <= upper.y();
        }
        boolean equals(Pixel other) {
            return x() == other.x() && y() == other.y();
        }
    }
    public record Pair<T, U>(T first, U second) {}

    public record Cell(Pixel[] pixels) {
        int width() {
            int minX = -1, maxX = -1;
            for (Pixel p : pixels) {
                if (minX == -1 || minX > p.x()) {
                    minX = p.x();
                }
                if (maxX == -1 || maxX < p.x()) {
                    maxX = p.x();
                }
            }
            return maxX - minX;
        }
        int height() {
            int minY = -1, maxY = -1;
            for (Pixel p : pixels) {
                if (minY == -1 || minY > p.y()) {
                    minY = p.y();
                }
                if (maxY == -1 || maxY < p.y()) {
                    maxY = p.y();
                }
            }
            return maxY - minY;
        }
        boolean contains(Pixel pixel) {
            for (Pixel p : pixels) {
                if (p.equals(pixel)) {
                    return true;
                }
            }
            return false;
        }

        Cell merge(Cell other) {
            final Pixel[] mergedPixels = new Pixel[pixels().length + other.pixels().length];
            
            int i;
            for (i = 0; i < pixels().length; i++) {
                mergedPixels[i] = pixels()[i];
            }
            System.arraycopy(other.pixels(), 0, mergedPixels, i, other.pixels().length);
            return new Cell(mergedPixels);
        }
    }
    public enum EdgeType { HORIZONTAL, VERTICAL }
    public record Edge(Pixel start, Pixel end) {
        Pixel midpoint() {
            return new Pixel((int) Math.floor((start.x() + end.x()) / 2), (int) Math.floor((start.y() + end.y()) / 2));
        }
        EdgeType type() {
            // assumption: the edge is valid
            if (start.x() == end.x()) {
                return EdgeType.VERTICAL;
            } 
            return EdgeType.VERTICAL;
        }
    }
    
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
                final Pixel[] pixels = new Pixel[(a + rH)*(b + rC)];
                for (int k = 0; k < a + rH; k++) {
                    for (int l = 0; l < b + rC; l++) {
                        // only add non-transparent pixels to the cell
                        if (image.getColorModel().hasAlpha() && ((image.getRGB(l + b*j, k + a*i) >> 24) & 0xff) == 255) {
                            pixels[k*b + l] = new Pixel(l + b*j, k + a*i);
                        }
                            
                    }
                }

                row[j] = new Cell(pixels);
            }
            grid[i] = row;
        }
        return grid;
    }

    public static Pair<Cell, Optional<Cell>> cut(Edge edge, Cell cellA, Cell cellB) {
        // if the edge is too small (i.e < half the expected length, we merge the cells)
        if (!isEdgeValid(edge, cellA) || !isCellValid(cellA) || !isCellValid(cellB)) {
            return new Pair<>(cellA.merge(cellB), Optional.empty());
        } 
        final int direction = Math.random() > 0.5 ? 1 : -1;
        final Pixel midpoint = edge.midpoint();
        Pixel upper, lower;
        switch (edge.type()) {
            case HORIZONTAL -> { 
                lower = midpoint.translate(0, -50);
                upper = midpoint.translate(direction * 50, 50);
            }
            case VERTICAL -> { 
                lower = midpoint.translate(-50, 0);
                upper = midpoint.translate(50, direction * 50);
            }
            default -> throw new IllegalArgumentException();
        }

        Pair<Cell, ArrayList<Pixel>> targets;
        if (direction == -1) {
            targets = removePixels(cellA, upper, lower);
            return new Pair<>(targets.first(), Optional.of(addPixels(cellB, targets.second())));
        } else {
            targets = removePixels(cellB, upper, lower);
            return new Pair<>(addPixels(cellA, targets.second()), Optional.of(targets.first()));
        }
    }
    private static Pair<Cell, ArrayList<Pixel>> removePixels(Cell cell, Pixel upper, Pixel lower) {
        final Pixel[] pixels = new Pixel[cell.pixels().length];
        final ArrayList<Pixel> pixelsRemoved = new ArrayList<>();
        for (int i = 0; i < cell.pixels().length; i++) {
            final Pixel pixel = cell.pixels()[i];
            if (!pixel.between(upper, lower)) {
                pixelsRemoved.add(pixel);
            } else {
                pixels[i] = pixel;
            }
        }
        return new Pair<>(new Cell(pixels), pixelsRemoved);
    }
    private static Cell addPixels(Cell cell, ArrayList<Pixel> pixels) {
        final Pixel[] pixelsAppended = new Pixel[cell.pixels().length + pixels.size()];
        System.arraycopy(cell.pixels(), 0, pixelsAppended, 0, cell.pixels().length);
        System.arraycopy(pixels.toArray(), 0, pixelsAppended, cell.pixels().length, pixels.size());
        return new Cell(pixelsAppended);
    }
    private static boolean isEdgeValid(Edge edge, Cell cell) {
        return cell.contains(edge.midpoint()) && (cell.contains(edge.start()) || cell.contains(edge.end()));
    }
    private static boolean isCellValid(Cell cell) {
        return cell.width() > 100 && cell.height() > 100;
    }
    private Puzzle() {}
}
