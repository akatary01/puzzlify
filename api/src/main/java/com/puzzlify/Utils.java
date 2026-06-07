package com.puzzlify;

import java.util.ArrayList;

import com.puzzlify.Puzzle.Cell;
import com.puzzlify.Puzzle.Pixel;

public class Utils {
    public static ArrayList<Pixel> getBorder(Cell cell, int borderSize) {
        final ArrayList<Pixel> borderPixels = new ArrayList<>();
        
        for (Pixel pixel : cell.pixels()) {
            if (isBorderPixel(cell, pixel)) {
                // set border pixels 
                for (int k = -borderSize; k <= borderSize; k++) {
                    for (int l = -borderSize; l <= borderSize; l++) {
                        final int neighborX = pixel.x() + k;
                        final int neighborY = pixel.y() + l;
                        if (neighborX < 0 || neighborX >= cell.width() || neighborY < 0 || neighborY >= cell.height()) {
                            continue;
                        }
                        borderPixels.add(new Pixel(neighborX, neighborY));
                    }
                }
            }
        }
        return borderPixels;
    }

    public static boolean isBorderPixel(Cell cell, Pixel pixel) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) { continue; }
                final Pixel neighbor = new Pixel(pixel.x() + i, pixel.y() + j);
                if (!cell.contains(neighbor)) {
                    return true;
                } 
            }
        }
        return false;
    }
}
