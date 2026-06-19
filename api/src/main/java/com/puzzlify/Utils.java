package com.puzzlify;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.jetbrains.annotations.NotNull;

import com.puzzlify.records.Cell;
import com.puzzlify.records.Utils.Pair;
import com.puzzlify.records.Utils.Pixel;

import io.javalin.http.UploadedFile;

public class Utils {

    public static BufferedImage toBufferedImage(@NotNull UploadedFile uploadedFile) throws IOException {
        try (InputStream is = uploadedFile.content()) {
            return ImageIO.read(is);
        }
    }

    public static Pair<BufferedImage, BufferedImage[][]> convertPuzzleToImage(BufferedImage image, Cell[][] puzzle) {
        final BufferedImage cutImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final BufferedImage[][] cells = new BufferedImage[puzzle.length][puzzle[0].length];
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
                final Cell piece = puzzle[i][j];
                if (piece == null || piece.size() == 0) {
                    continue;
                }
                Pair<Pixel, Pixel> bounds = piece.boundingBox();
                BufferedImage cell = new BufferedImage(piece.width() + 1, piece.height() + 1, BufferedImage.TYPE_INT_ARGB);
                for (final Pixel pixel : piece.pixels()) {
                    // only color in non-transparent pixels
                    if (pixel != null) {
                        final int color;
                        if (piece.isBorderPixel(pixel)) {
                            color = 0xFF000000;
                        } else {
                            color = image.getRGB(pixel.x(), pixel.y());
                        }
                        // set border pixels to black
                        cutImage.setRGB(pixel.x(), pixel.y(), color);
                        cell.setRGB(pixel.x() - bounds.first().x(), pixel.y() - bounds.first().y(),
                                color);
                    }
                }
                cells[i][j] = cell;
            }
        }
        return new Pair<>(cutImage, cells);
    }
}
