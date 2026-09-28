package com.puzzlify.records;

import org.jetbrains.annotations.NotNull;

public class Utils {
    public enum CutType { CIRCLE, SQUARE, TRIANGLE, QAUDRATIC, CUBIC }
    public record Pair<T, U>(@NotNull T first, @NotNull U second) {}
    public record Pixel(int x, int y) {
        public Pixel translate(int deltaX, int deltaY) {
            return new Pixel(x() + deltaX, y() + deltaY);
        }
        public boolean inside(Edge edge, double radius, int direction, CutType cutType) {
            final Pixel midpoint = edge.midpoint();
            // circle
            final boolean valid;
            final boolean inside;
            switch (cutType) {
                case SQUARE:
                    inside = (midpoint.x() - radius <= x()) && (x() <= midpoint.x() + radius) && (midpoint.y() - radius <= y()) && (y() <= midpoint.y() + radius);
                    break;
                case TRIANGLE:
                    inside = switch (edge.type()) {
                        case HORIZONTAL -> Math.abs(x - midpoint.x()) -direction * (midpoint.y() + direction * radius) <= -direction * y();
                        case VERTICAL -> -direction * (x() - midpoint.x() - direction * radius) + midpoint.y() >= y() && y() >= direction * (x() - midpoint.x() - direction * radius) + midpoint.y();
                    };
                    break;
                // case QAUDRATIC:
                //     inside = switch (edge.type()) {
                //         case HORIZONTAL -> -direction * y() >= (midpoint.y() / Math.pow(radius, 2)) * (x() - midpoint.x() - radius) * (x() - midpoint.x() + radius) - direction * radius;
                //         case VERTICAL -> -direction * x() >= (midpoint.x() / Math.pow(radius, 2)) * (y() - midpoint.y() - radius) * (y() - midpoint.y() + radius)  - direction * radius;
                //     };
                //     break;
                // case CUBIC:
                //     // TODO: fix
                //     // inside = -8 / 3 * (midpoint.y() + radius) / Math.pow(radius, 3) * (x() - midpoint.x()) * (x() - (midpoint.x() - radius)) * (x() - (midpoint.x() + radius)) >= direction * y();
                //     // break;
                case CIRCLE: 
                default:
                    inside = Math.pow(x() - midpoint.x(), 2) + Math.pow(y() - midpoint.y(), 2) <= Math.pow(radius, 2);
                    break;
            }
            valid = edge.type() == EdgeType.VERTICAL ? direction * x() >= direction * midpoint.x() : direction * y() >= direction * midpoint.y();
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
