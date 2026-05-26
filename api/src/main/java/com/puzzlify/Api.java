package com.puzzlify;

import io.javalin.Javalin;

public class Api {
    public static void main(String[] args) {
        var api = Javalin.create(/*config*/)
            .get("/puzzlify", ctx -> {
                // the image, rows and cols should come from the request
                throw new UnsupportedOperationException("Not implemented yet");
                // TODO: Eman 

                // TODO: store the images and return the urls so the frontend can fetch them 
                // and display and the user can download.
                // ctx.json([urls])
            })
            .start(7070);
    }
}