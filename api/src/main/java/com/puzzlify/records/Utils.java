package com.puzzlify.records;

import org.jetbrains.annotations.NotNull;

public class Utils {
    public record Pair<T, U>(@NotNull T first, @NotNull U second) {}
    public record Pixel(int x, int y) {
        public Pixel translate(int deltaX, int deltaY) {
            return new Pixel(x() + deltaX, y() + deltaY);
        }
        public boolean inside(Edge edge, double radius, int direction) {
            final Pixel midpoint = edge.midpoint();
            // circle
            final boolean inside = Math.pow(x() - midpoint.x(), 2) + Math.pow(y() - midpoint.y(), 2) <= Math.pow(radius, 2);
            // square 
            // final boolean inside = (midpoint.x() - radius <= x()) && (x() <= midpoint.x() + radius) && (midpoint.y() - radius <= y()) && (y() <= midpoint.y() + radius);
            // triangle 
            // final boolean inside = -1 / radius * Math.pow(x() - midpoint.x(), 2) + direction * midpoint.y() + radius >= direction * y();
            // parabola
            // final boolean inside = -Math.abs(x() - midpoint.x()) + direction * midpoint.y() + radius >= direction * y();
            // cubic
            // final boolean inside = -8 / 3 * (midpoint.y() + radius) / Math.pow(radius, 3) * (x() - midpoint.x()) * (x() - (midpoint.x() - radius)) * (x() - (midpoint.x() + radius)) >= direction * y();

            // final boolean insideSquare = (midpoint.x() - radius * 3/4 <= x()) && (x() <= midpoint.x() + radius * 3/4) && (midpoint.y() - 1 / 2 * radius <= y()) && (y() <= midpoint.y() + 1 / 2 * radius);
            // final boolean insideCircle = Math.pow(x() - midpoint.x(), 2) + Math.pow(y() - (midpoint.y() + radius * 1 / 2), 2) <= Math.pow(radius * 1 / 2, 2);
            // final boolean inside = insideSquare && insideCircle;
            final boolean valid = edge.type() == EdgeType.VERTICAL ? direction * x() >= direction * midpoint.x() : direction * y() >= direction * midpoint.y();
            return inside && valid;
        }
    }

    public enum EdgeType { HORIZONTAL, VERTICAL }
    public record Edge(@NotNull Pixel start, @NotNull Pixel end) {
        public Pixel midpoint() {
            return new Pixel(((start.x() + end.x()) / 2), ((start.y() + end.y()) / 2));
        }
        public EdgeType type() {
            // assumption: the edge is valid
            if (start.x() == end.x()) {
                return EdgeType.VERTICAL;
            } 
            return EdgeType.HORIZONTAL;
        }
    }
}
