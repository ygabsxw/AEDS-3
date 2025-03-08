package file;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Movie;

public class readCSV {
    private final File file;

    public readCSV(String filename) {
        this.file = new File(filename);
    }

    public String getFilePath() {
        return file.getPath();
    }

    public List<Movie> read() throws IOException {
        List<Movie> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // pular o cabecalho do CSV
                }
                var movies = parseCSVMovie(line);
                data.add(movies);
            }
        } 
        return data;
    }

    public static Movie parseCSVMovie(String line) {
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        int id = Integer.parseInt(parts[0]);
        Movie movie = new Movie(id);
        movie.setType(parts[1]);
        movie.setTitle(parts[2]);
        movie.setDirector(parts[3]);
        movie.setCast(parts[4].isEmpty() ? new String[0] : parts[4].replace("\"", "").split(", "));
        movie.setCountry(parts[5]);
        movie.setDateAdded(parts[6].replace("\"", ""));
        movie.setReleaseYear(Integer.parseInt(parts[7]));
        movie.setRating(parts[8]);
        movie.setDuration(parts[9].split(" ")[0]); // Extrair apenas o número
        movie.setListedIn(parts[10]);
        movie.setDescription(parts[11]);
        return movie;
    }
}
