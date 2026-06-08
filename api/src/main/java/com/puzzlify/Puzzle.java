package com.puzzlify;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.puzzlify.records.Cell;
import com.puzzlify.records.Utils.Edge;
import com.puzzlify.records.Utils.Pair;
import com.puzzlify.records.Utils.Pixel;

public class Puzzle {
    public static Cell[][] puzzlify(final BufferedImage image, final int rows, final int cols) {
        final Cell[][] grid = constructGrid(image, rows, cols, false);
        final Cell[][] puzzle = constructGrid(image, rows, cols, true);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // skip transparent cells
                if (puzzle[i][j].size() == 0) { continue; }
                System.out.println(String.format("Cell %s,%s has %s pixels", i, j, puzzle[i][j].size()));
                final Pair<Pixel, Pixel> boundA = grid[i][j].boundingBox();

                final int a = image.getHeight() / rows;
                final int b = image.getWidth() / cols;

                final Pixel topLeft = boundA.first();
                final Pixel bottomRight = boundA.second();

                // we do not process the last set of horizontal edges
                if (i != rows - 1) {
                    final Edge edgeH = new Edge(topLeft.translate(0, a - 1), bottomRight);

                    // horizontal cut and update cells
                    final double cutSize = 0.125 * b;
                    Pair<Cell, Optional<Cell>> cutH = cut(edgeH, puzzle[i][j], puzzle[i + 1][j], cutSize);
                    puzzle[i][j] = cutH.first();
                    puzzle[i + 1][j] = cutH.second().orElse(null);
                }

                // we do not process the last set of vertical edges
                if (j != cols - 1) {
                    final Edge edgeV = new Edge(topLeft.translate(b - 1, 0), bottomRight);

                    //vertical cut and update cells
                    final double cutSize = 0.125 * a;
                    Pair<Cell, Optional<Cell>> cutV = cut(edgeV, puzzle[i][j], puzzle[i][j + 1], cutSize);
                    puzzle[i][j] = cutV.first();
                    puzzle[i][j + 1] = cutV.second().orElse(null);
                }
            }
        }

        return puzzle;
    }

    private static Cell[][] constructGrid(final BufferedImage image, final int rows, final int cols, final boolean transparent) {
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
                final Set<Pixel> pixels = new HashSet<>();
                for (int k = 0; k < a + rH; k++) {
                    for (int l = 0; l < b + rC; l++) {
                        // only add non-transparent pixels to the cell
                        if (!transparent || (!image.getColorModel().hasAlpha()
                                || (image.getColorModel().hasAlpha()
                                && ((image.getRGB(l + b * j, k + a * i) >> 24) & 0xff) == 255))) {
                            pixels.add(new Pixel(l + b * j, k + a * i));
                        }
                    }
                }
                row[j] = new Cell(pixels);
            }
            grid[i] = row;
        }
        return grid;
    }

    private static Pair<Cell, Optional<Cell>> cut(Edge edge, @NotNull Cell cellA, Cell cellB, double cutSize) {
        if (cellB == null) {
            return new Pair<>(cellA, Optional.empty());
        }

        final int direction = cellA.size() < cellB.size() ? 1 : -1;
        final Pair<Cell, Set<Pixel>> targets;
        if (direction == -1) {
            targets = cellA.cut(edge, cutSize, direction);
            // System.out.println(String.format("targets: (, %d)", targets.second().size()));
            return new Pair<>(targets.first(), Optional.of(cellB.extend(targets.second())));
        } else {
            targets = cellB.cut(edge, cutSize, direction);
            // System.out.println(String.format("targets: (, %d)", targets.first().size()));
            return new Pair<>(cellA.extend(targets.second()), Optional.of(targets.first()));
        }
    }

    private Puzzle() {}
}
