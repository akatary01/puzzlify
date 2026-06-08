package com.puzzlify.records;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.puzzlify.records.Utils.Edge;
import com.puzzlify.records.Utils.Pair;
import com.puzzlify.records.Utils.Pixel;

public record Cell(@NotNull Set<Pixel> pixels) {
    public Cell extend(Set<Pixel> newPixels) {
        final Set<Pixel> extendedPixels = new HashSet<>(pixels());
        extendedPixels.addAll(newPixels);
        return new Cell(extendedPixels);
    }
    public Pair<Cell, Set<Pixel>> cut(Edge edge, double radius, int direction) {
        final Set<Pixel> cutPixels = new HashSet<>();
        // int contained = 0;
        final Set<Pixel> pixelsRemoved = new HashSet<>();
        for (final Pixel pixel : pixels()) {
            if (pixel == null) {
                continue;
            }
            if (pixel.inside(edge, radius, direction)) {
                // contained++;
                pixelsRemoved.add(pixel);
            } else {
                cutPixels.add(pixel);
            }
        }
        // System.out.println(String.format("contained: %s / %s", contained, pixels().size()));
        return new Pair<>(new Cell(cutPixels), pixelsRemoved);
    }
    /** Returns the bounding box of the cell as a pair of pixels (top-left and bottom-right). */
    public Pair<Pixel, Pixel> boundingBox() {
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
    public int size() {
        int i = 0;
        for (Pixel p : pixels()) {
            if (p != null) { i++; }
        }
        return i;
    }
    public int width() {
        final Pair<Pixel, Pixel> B = boundingBox();
        return B.second().x() - B.first().x();
    }
    public int height() {
        final Pair<Pixel, Pixel> B = boundingBox();
        return B.second().y() - B.first().y();
    }
    public boolean contains(Pixel pixel) {
        return pixels().contains(pixel);
    }
    public Set<Pixel> border(int borderSize) {
        final Set<Pixel> borderPixels = new HashSet<>();
        
        for (Pixel pixel : pixels()) {
            if (isBorderPixel(pixel)) {
                // set border pixels 
                for (int k = -borderSize; k <= borderSize; k++) {
                    for (int l = -borderSize; l <= borderSize; l++) {
                        final int neighborX = pixel.x() + k;
                        final int neighborY = pixel.y() + l;
                        if (neighborX < 0 || neighborX >= width() || neighborY < 0 || neighborY >= height()) {
                            continue;
                        }
                        borderPixels.add(new Pixel(neighborX, neighborY));
                    }
                }
            }
        }
        return borderPixels;
    }
    public boolean isBorderPixel(Pixel pixel) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) { continue; }
                final Pixel neighbor = new Pixel(pixel.x() + i, pixel.y() + j);
                if (!contains(neighbor)) {
                    return true;
                } 
            }
        }
        return false;
    }
}
