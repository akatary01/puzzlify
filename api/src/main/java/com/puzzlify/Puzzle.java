package com.puzzlify;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class Puzzle {
    public record Pixel(int x, int y) {
        Pixel translate(int deltaX, int deltaY) {
            return new Pixel(x() + deltaX, y() + deltaY);
        }
        double distance(@NotNull Pixel pixel) {
            return Math.sqrt(Math.pow(x() - pixel.x(), 2) + Math.pow(y() - pixel.y(), 2));
        }
    }
    public record Pair<T, U>(@NotNull T first, @NotNull U second) {}

    public record Cell(@NotNull Pixel[] pixels) {
        Pair<Pixel, Pixel> boundingBox() {
            int minX = -1, maxX = -1, minY = -1, maxY = -1;
            for (Pixel p : pixels()) {
                if (p == null) { continue; }
                if (minX == -1 || minX > p.x()) { minX = p.x(); }
                if (maxX == -1 || maxX < p.x()) { maxX = p.x(); }
                if (minY == -1 || minY > p.y()) { minY = p.y(); }
                if (maxY == -1 || maxY < p.y()) { maxY = p.y(); }
            }
            return new Pair<>(new Pixel(minX, minY), new Pixel(maxX, maxY));
        }
        int size() {
            int i = 0;
            for (Pixel p : pixels()) {
                if (p != null) { i++; }
            }
            return i;
        }
        int width() {
            final Pair<Pixel, Pixel> B = boundingBox();
            return B.second().x() - B.first().x();
        }
        int height() {
            final Pair<Pixel, Pixel> B = boundingBox();
            return B.second().y() - B.first().y();
        }
        Pixel closest(@NotNull Pixel pixel) {
            double minD = Double.MAX_VALUE;
            Pixel closestP = null;
            for (Pixel p : pixels()) {
                if (p == null) { continue; }

                double d = p.distance(pixel);
                if (p.distance(pixel) < minD) {
                    minD = d; closestP = p;
                }
            }
            return closestP;
        }
    }

    public enum EdgeType { HORIZONTAL, VERTICAL }
    public record Edge(@NotNull Pixel start, @NotNull Pixel end) {
        Pixel midpoint() {
            return new Pixel(((start.x() + end.x()) / 2), ((start.y() + end.y()) / 2));
        }
        EdgeType type() {
            // assumption: the edge is valid
            if (start.x() == end.x()) {
                return EdgeType.VERTICAL;
            } 
            return EdgeType.HORIZONTAL;
        }
    }

    public static Cell[][] constructGrid(final BufferedImage image, final int rows, final int cols, final boolean transparent) {
        final Cell[][] grid = new Cell[rows][cols];
        
        final int h = image.getHeight();
        final int w = image.getWidth();
        
        final int a = h / rows;
        final int b = w / cols;
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
                        if (
                            !transparent || (
                                !image.getColorModel().hasAlpha() ||
                                (
                                    image.getColorModel().hasAlpha() && 
                                    ((image.getRGB(l + b*j, k + a*i) >> 24) & 0xff) == 255
                                )
                            )
                        ) {
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

    public static Pair<Cell, Optional<Cell>> cut(Edge edge, @NotNull Cell cellA, Cell cellB, double cutSize) {
        // if the edge is too small (i.e < half the expected length, we merge the cells)
        // if one of the cells is too small we merge the cells
        if (cellB == null) { 
            return new Pair<>(cellA, Optional.empty()); 
        }
        
        final int direction = cellA.size() < cellB.size() ? 1 : -1;
        final Pair<Cell, ArrayList<Pixel>> targets;
        if (direction == -1) {
            targets = removePixels(cellA, edge, cutSize, direction);
            System.out.println(String.format("targets: (, %d)", targets.second().size()));
            return new Pair<>(targets.first(), Optional.of(addPixels(cellB, targets.second())));
        } else {
            targets = removePixels(cellB, edge, cutSize, direction);
            System.out.println(String.format("targets: (, %d)", targets.first().size()));
            return new Pair<>(addPixels(cellA, targets.second()), Optional.of(targets.first()));
        }
    }
    private static Pair<Cell, ArrayList<Pixel>> removePixels(Cell cell, Edge edge, double radius, int direction){
        final Pixel[] pixels = new Pixel[cell.pixels().length];
        int contained = 0;
        final ArrayList<Pixel> pixelsRemoved = new ArrayList<>();
        for (int i = 0; i < cell.pixels().length; i++) {
            final Pixel pixel = cell.pixels()[i];
            if (pixel == null) continue;
            if (contains(pixel, edge, radius, direction)) {
                contained++;
                pixelsRemoved.add(pixel);
            } else {
                pixels[i] = pixel;
            }
        }
        System.out.println(String.format("contained: %s / %s" , contained, cell.pixels().length));
        return new Pair<>(new Cell(pixels), pixelsRemoved);
    }
    private static boolean contains(Pixel pixel, Edge edge, double radius, int direction) {
        final Pixel midpoint = edge.midpoint();
        // circle
        final boolean inside = Math.pow(pixel.x() - midpoint.x(), 2) + Math.pow(pixel.y() - midpoint.y(), 2) <= Math.pow(radius, 2);
        // square 
        // final boolean inside = (midpoint.x() - radius <= pixel.x()) && (pixel.x() <= midpoint.x() + radius) && (midpoint.y() - radius <= pixel.y()) && (pixel.y() <= midpoint.y() + radius);
        // triangle 
        // final boolean inside = -1 / radius * Math.pow(pixel.x() - midpoint.x(), 2) + direction * midpoint.y() + radius >= direction * pixel.y();
        // parabola
        // final boolean inside = -Math.abs(pixel.x() - midpoint.x()) + direction * midpoint.y() + radius >= direction * pixel.y();

        final boolean valid = edge.type() == EdgeType.VERTICAL ? direction * pixel.x() >= direction * midpoint.x() : direction * pixel.y() >= direction * midpoint.y();
        return inside && valid;
    }
    private static Cell addPixels(Cell cell, ArrayList<Pixel> pixels) {
        final Pixel[] pixelsAppended = new Pixel[cell.pixels().length + pixels.size()];
        System.arraycopy(cell.pixels(), 0, pixelsAppended, 0, cell.pixels().length);
        System.arraycopy(pixels.toArray(), 0, pixelsAppended, cell.pixels().length, pixels.size());
        return new Cell(pixelsAppended);
    }
    private Puzzle() {}
}
