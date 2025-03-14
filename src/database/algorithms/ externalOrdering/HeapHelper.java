
import model.Movie;

public class HeapHelper {
    Movie movie;
    int origin; // Índice do arquivo temporário de onde veio este filme

    public HeapHelper(Movie movie, int origin) {
        this.movie = movie;
        this.origin = origin;
    }
}