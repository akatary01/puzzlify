package com.puzzlify;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.imageio.ImageIO;

import static com.puzzlify.Puzzle.puzzlify;
import static com.puzzlify.Utils.convertPuzzleToImage;
import static com.puzzlify.Utils.toBufferedImage;
import com.puzzlify.records.Cell;
import com.puzzlify.records.Utils.Pair;

import io.javalin.Javalin;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;

public class Api {

    public static void main(String[] args) throws IOException {
        var api = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/puzzles";
                staticFiles.directory = "./puzzles";
                staticFiles.location = Location.EXTERNAL;
        });
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.allowHost("http://localhost:8000/");
                });
            });
        }
        );
        api
            .post("/puzzlify/", ctx -> {
                int rows = Integer.parseInt(ctx.queryParam("rows"));
                int cols = Integer.parseInt(ctx.queryParam("cols"));
                UploadedFile uploadedImage = ctx.uploadedFile("image");

                if (uploadedImage == null || rows == 0 || cols == 0) {
                    return;
                }

                BufferedImage image = toBufferedImage(uploadedImage);
                final Cell[][] puzzle = puzzlify(image, rows, cols);
                final Pair<BufferedImage, BufferedImage[][]> puzzleImage = convertPuzzleToImage(image, puzzle);
                
                final BufferedImage cutImage = puzzleImage.first();
                // final BufferedImage[][] pieces = puzzleImage.second();

                // generate the image with the cut pattern
                final UUID uid = UUID.randomUUID();
                final File cutImageFile = new File(String.format("./puzzles/%s.png", uid));
                ImageIO.write(cutImage, "png", cutImageFile);
                
                ctx.result(String.format("http://localhost:7070/puzzles/%s.png", uid));
            })
            .start(7070);
    }
}
