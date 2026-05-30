package com.puzzlify;

import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class Puzzle {
    private static final double MIN_VALID_EDGE_THRESHOLD = 0.2;

    public record Pixel(int x, int y) {
        Pixel translate(int deltaX, int deltaY) {
            return new Pixel(x() + deltaX, y() + deltaY);
        }
        boolean between(@NotNull Pixel A, @NotNull Pixel B) {
            int a = Math.min(A.x(), B.x()); int b = Math.min(A.y(), B.y());
            int c = Math.max(A.x(), B.x()); int d = Math.max(A.y(), B.y());
            return a <= x() && x() <= c && b <= y() && y() <= d;
        }
        boolean equals(@NotNull Pixel other) {
            return x() == other.x() && y() == other.y();
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
        boolean contains(@NotNull Pixel pixel) {
            for (Pixel p : pixels()) {
                if (p != null && p.equals(pixel)) {
                    return true;
                }
            }
            return false;
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
        Cell merge(@NotNull Cell other) {
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
    
    public static Cell[][] constructGrid(final BufferedImage image, final int rows, final int cols) {
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
                            !image.getColorModel().hasAlpha() ||
                            (
                                image.getColorModel().hasAlpha() && 
                                ((image.getRGB(l + b*j, k + a*i) >> 24) & 0xff) == 255
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

    public static Pair<Cell, Optional<Cell>> cut(Edge edge, @NotNull Cell cellA, Cell cellB, int avgH, int avgW) {
        // if the edge is too small (i.e < half the expected length, we merge the cells)
        // if one of the cells is too small we merge the cells
        if (cellB == null) { 
            return new Pair<>(cellA, Optional.empty()); 
        }

        edge = new Edge(cellA.closest(edge.start()), cellA.closest(edge.end()));
        int direction = cellA.size() < cellB.size() ? 1 : -1;
        final Pixel midpoint = edge.midpoint();
        Pixel upper, lower;

        double edgeLength = edge.start().distance(edge.end());
        int cutSize = (int) (0.25 * edgeLength);
        switch (edge.type()) {
            case HORIZONTAL -> { 
                lower = midpoint.translate(-cutSize, 0);
                upper = midpoint.translate(cutSize, direction * ((int) ((0.35 * Math.random() + 0.15) * cellA.height())));
            }
            case VERTICAL -> { 
                lower = midpoint.translate(0, -cutSize);
                upper = midpoint.translate(direction * ((int) ((0.35 * Math.random() + 0.15) * cellA.width())), cutSize);
            }
            default -> throw new IllegalArgumentException();
        }
        System.out.println(String.format("(lower, upper): %s, %s", lower, upper));
        final Pair<Cell, ArrayList<Pixel>> targets;
        final CubicCurve2D curve = curvedEdge(edge, upper, lower);
        if (direction == -1) {
            targets = removePixels(cellA, curve, edge);
            System.out.println(String.format("targets: (, %d)", targets.second().size()));
            return new Pair<>(targets.first(), Optional.of(addPixels(cellB, targets.second())));
        } else {
            targets = removePixels(cellB, curve, edge);
            System.out.println(String.format("targets: (, %d)", targets.first().size()));
            return new Pair<>(addPixels(cellA, targets.second()), Optional.of(targets.first()));
        }
    }
    private static CubicCurve2D curvedEdge(Edge edge, Pixel upper, Pixel lower) {
        return new CubicCurve2D.Double(
            edge.start().x(), edge.start().y(), 
            lower.x(), lower.y(), 
            upper.x(), upper.y(), 
            edge.end().x(), edge.end().y()
        );
    }
    private static Pair<Cell, ArrayList<Pixel>> removePixels(Cell cell, CubicCurve2D curve, Edge edge) {
        final Pixel[] pixels = new Pixel[cell.pixels().length];
        final ArrayList<Pixel> pixelsRemoved = new ArrayList<>();
        for (int i = 0; i < cell.pixels().length; i++) {
            final Pixel pixel = cell.pixels()[i];
            if (pixel == null) continue;
            if (curve.contains(pixel.x(), pixel.y()) || pixel.between(edge.start(), edge.end())) {
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
    private static boolean isEdgeValid(Edge edge, int avgH, int avgW) {
        return edge.start().distance(edge.end()) > MIN_VALID_EDGE_THRESHOLD * (edge.type() == EdgeType.VERTICAL ? avgH : avgW);
    }
    private static boolean isCellValid(Cell cell, int avgH, int avgW) {
        return cell.width() > MIN_VALID_EDGE_THRESHOLD * avgW  && cell.height() > MIN_VALID_EDGE_THRESHOLD * avgH;
    }
    private Puzzle() {}
}
