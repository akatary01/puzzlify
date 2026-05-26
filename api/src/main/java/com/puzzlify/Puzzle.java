package com.puzzlify;

public class Puzzle {
    public record Point(int x, int y) {}
    public record Pair<T, U>(T first, U second) {}

    public record Cell(Point[] coordinates) {}
    public record Edge(Point start, Point end) {}
    
    public static Cell[] constructGrid(int[][] image, int rows, int cols) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public static Pair<Cell, Cell> cut(Edge edge, Cell cellA, Cell cellB) {
        throw new UnsupportedOperationException("Not implemented yet");
        // TODO: Ahmed
    }
    private Puzzle() {}
}
