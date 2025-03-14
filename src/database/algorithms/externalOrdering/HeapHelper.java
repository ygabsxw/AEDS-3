package database.algorithms.externalOrdering;

import java.io.IOException;

import model.Movie;

public class HeapHelper {
    Movie movie;
    int origin; // Índice do arquivo temporário de onde veio este filme

    public HeapHelper(Movie movie, int origin) {
        this.movie = movie;
        this.origin = origin;
    }

     public int getId(){

        return this.movie.getId();

    }

    public int getOrigem(){

        return this.origin;

    }

    public byte[] toByteArray() throws IOException{

        return this.movie.toByteArray();

    }
}