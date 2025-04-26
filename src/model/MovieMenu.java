package model;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import database.algorithms.externalOrdering.ExternalSort;
import file.*;
import file.hash.FileManagerHash;
import file.btree.FileManagerArvoreB;
import file.inverted.FileManagerListaInvertida;

public class MovieMenu {
    FileManager<Movie> movieFile;
    private static Scanner sc = new Scanner(System.in);

    public MovieMenu(String fileType) throws Exception {
        switch(fileType.toLowerCase()) {
            case "sequential":
                movieFile = new FileManagerSequencial<>("movies_seq", Movie.class.getConstructor());
                break;
            case "hash":
                movieFile = new FileManagerHash<>("movies_hash", Movie.class.getConstructor());
                break;
            case "btree":
                movieFile = new FileManagerArvoreB<>("movies_btree", Movie.class.getConstructor());
                break;
            case "inverted":
                movieFile = new FileManagerListaInvertida<>("movies_inv", Movie.class.getConstructor());
                break;
            default:
                throw new IllegalArgumentException("Invalid file type");
        }
    }

    public void menu() {

        int option;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println(" 1 - Find By ID");
            System.out.println(" 2 - Find By Type - For Inverted List");
            System.out.println(" 3 - Include");
            System.out.println(" 4 - Change");
            System.out.println(" 5 - Delete");
            System.out.println(" 6 - List All");
            System.out.println(" 7 - Order All - For Sequential File");
            System.out.println(" 0 - Back");

            System.out.print("\nOption: ");
            try {
                option = Integer.valueOf(sc.nextLine());
            } catch(NumberFormatException e) {
                option = -1;
            }

            switch (option) {
                case 1:
                    findMovie();
                    break;
                case 2:
                    findByType();
                break;
                case 3:
                    includeMovie();
                    break;
                case 4:
                    changeMovie();
                    break;
                case 5:
                    deleteMovie();
                    break;
                case 6:
                    listAllMovies();
                    break;
                case 7:
                    orderAllMovies();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option!");
                    break;
            }

        } while (option != 0);
    }

     public void findMovie() {
        System.out.print("\nMovie ID: ");
        int id = sc.nextInt();  // Lê o ID digitado pelo usuário
        sc.nextLine();  // Limpar o buffer após o nextInt()

        if(id>0) {            
            try {
                Movie movie = movieFile.read(id);  // Chama o método de leitura da classe Arquivo
                if (movie != null) {
                    showMovie(movie);  // Exibe os detalhes do filme encontrado
                } else {
                    System.out.println("Movie not found.");
                }
            } catch(Exception e) {
                System.out.println("System error. Unable to search for the movie!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid ID.");
        }
    }   

    public void findByType() {
        System.out.print("\nMovie type (e.g., Movie or TV Show): ");
        String type = sc.nextLine();
    
        if (!type.isEmpty()) {
            try {
                List<Movie> movies = ((FileManagerListaInvertida<Movie>) movieFile).searchByType(type);
                if (movies != null && !movies.isEmpty()) {
                    for (Movie movie : movies) {
                        showMovie(movie);
                    }
                } else {
                    System.out.println("No movies found for the given type.");
                }
            } catch (Exception e) {
                System.out.println("System error. Unable to search for the movie!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid type.");
        }
    }


    public void includeMovie() {
        String type = "";
        String title = "";
        String director = "";
        String[] cast = null;
        String country = "";
        String date_added = "";
        Integer release_year = null;
        String rating = "";
        String duration = "";
        String listed_in = "";
        String description = "";
    
        System.out.println("\nMovie inclusion");
        
        // Type
        do {
            System.out.print("\nType (min. 4 letters or empty to cancel): ");
            type = sc.nextLine();
            if(type.length()==0)
                return;
            if(type.length()<4)
                System.err.println("The film type must have at least 4 characters.");
        } while(type.length()<4);
    
        // Title
        System.out.print("Title: ");
        title = sc.nextLine();
    
        // Director
        System.out.print("Director: ");
        director = sc.nextLine();
    
        // Cast
        System.out.print("Cast (comma-separated): ");
        String castInput = sc.nextLine();
        cast = castInput.isEmpty() ? new String[0] : castInput.split(",");
    
        // Country
        System.out.print("Country: ");
        country = sc.nextLine();
    
        // Date Added
        do {
            System.out.print("Date added (Month Day, Year): ");
            date_added = sc.nextLine();
            if (!date_added.matches("\\w+ \\d{1,2}, \\d{4}")) {
                System.err.println("Invalid date format. Use 'Month Day, Year'.");
            }
        } while (!date_added.matches("\\w+ \\d{1,2}, \\d{4}"));
    
        // Release Year
        do {
            System.out.print("Release year: ");
            try {
                release_year = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.err.println("Invalid year. Please enter a valid number.");
            }
        } while (release_year == null);
    
        // Rating
        System.out.print("Rating: ");
        rating = sc.nextLine();
    
        // Duration
        System.out.print("Duration (e.g., 90 min): ");
        duration = sc.nextLine();
    
        // Listed In
        System.out.print("Listed in: ");
        listed_in = sc.nextLine();
    
        // Description
        System.out.print("Description: ");
        description = sc.nextLine();
    
        System.out.print("\nConfirm movie inclusion? (Y/N) ");
        char resp = sc.nextLine().toUpperCase().charAt(0);
        if(resp == 'Y') {
            try {
                Movie m = new Movie(movieFile.getNextId(), type, title, director, cast, country, date_added, release_year, rating, duration, listed_in, description);
                movieFile.create(m);
                System.out.println("Movie included successfully.");
            } catch(Exception e) {
                System.out.println("System error. Unable to include the movie!");
                e.printStackTrace();
            }
        }
    }
    

    public void changeMovie() {
        System.out.print("\nEnter the movie ID to be changed: ");
        int id = sc.nextInt();
        sc.nextLine(); // Limpar o buffer após o nextInt()
    
        if (id > 0) {
            try {
                Movie movie = movieFile.read(id);
                if (movie != null) {
                    System.out.println("Movie found:");
                    showMovie(movie); // Assume que você tem um método para exibir os detalhes do filme
    
                    // Tipo
                    System.out.print("\nNew type (leave blank to keep current): ");
                    String newType = sc.nextLine();
                    if (!newType.isEmpty()) {
                        movie.setType(newType);
                    }
    
                    // Título
                    System.out.print("New title (leave blank to keep current): ");
                    String newTitle = sc.nextLine();
                    if (!newTitle.isEmpty()) {
                        movie.setTitle(newTitle);
                    }
    
                    // Diretor
                    System.out.print("New director (leave blank to keep current): ");
                    String newDirector = sc.nextLine();
                    if (!newDirector.isEmpty()) {
                        movie.setDirector(newDirector);
                    }
    
                    // Elenco
                    System.out.print("New cast (comma-separated, leave blank to keep current): ");
                    String newCastInput = sc.nextLine();
                    if (!newCastInput.isEmpty()) {
                        movie.setCast(newCastInput.split(","));
                    }
    
                    // País
                    System.out.print("New country (leave blank to keep current): ");
                    String newCountry = sc.nextLine();
                    if (!newCountry.isEmpty()) {
                        movie.setCountry(newCountry);
                    }
    
                    // Data de adição
                    System.out.print("New date added (Month Day, Year, leave blank to keep current): ");
                    String newDateAdded = sc.nextLine();
                    if (!newDateAdded.isEmpty()) {
                        movie.setDateAdded(newDateAdded);
                    }
    
                    // Ano de lançamento
                    System.out.print("New release year (leave blank to keep current): ");
                    String newReleaseYearStr = sc.nextLine();
                    if (!newReleaseYearStr.isEmpty()) {
                        try {
                            movie.setReleaseYear(Integer.parseInt(newReleaseYearStr));
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid year. Current value kept.");
                        }
                    }
    
                    // Classificação
                    System.out.print("New rating (leave blank to keep current): ");
                    String newRating = sc.nextLine();
                    if (!newRating.isEmpty()) {
                        movie.setRating(newRating);
                    }
    
                    // Duração
                    System.out.print("New duration (leave blank to keep current): ");
                    String newDuration = sc.nextLine();
                    if (!newDuration.isEmpty()) {
                        movie.setDuration(newDuration);
                    }
    
                    // Listado em
                    System.out.print("New listed in (leave blank to keep current): ");
                    String newListedIn = sc.nextLine();
                    if (!newListedIn.isEmpty()) {
                        movie.setListedIn(newListedIn);
                    }
    
                    // Descrição
                    System.out.print("New description (leave blank to keep current): ");
                    String newDescription = sc.nextLine();
                    if (!newDescription.isEmpty()) {
                        movie.setDescription(newDescription);
                    }
    
                    // Confirmação da alteração
                    System.out.print("\nConfirm changes? (Y/N) ");
                    char resp = sc.next().charAt(0);
                    if (resp == 'Y' || resp == 'y') {
                        boolean updated = movieFile.update(movie);
                        if (updated) {
                            System.out.println("Movie updated successfully.");
                        } else {
                            System.out.println("Error updating the movie.");
                        }
                    } else {
                        System.out.println("Changes cancelled.");
                    }
                } else {
                    System.out.println("Movie not found.");
                }
            } catch (Exception e) {
                System.out.println("System error. Unable to change the movie!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid ID.");
        }
    }
    


    public void deleteMovie() {
        System.out.print("\nEnter the ID of the movie to be deleted: ");
        int id = sc.nextInt();  // Lê o ID digitado pelo usuário

        if (id > 0) {
            try {
                // Tenta ler o filme com o ID fornecido
                Movie movie = movieFile.read(id);
                if (movie != null) {
                    System.out.println("Movie found:");
                    showMovie(movie);  // Exibe os dados do filme para confirmação

                    System.out.print("\nDo you confirm the deletion of the movie? (Y/N) ");
                    char resp = sc.next().charAt(0);  // Lê a resposta do usuário

                    if (resp == 'Y' || resp == 'y') {
                        boolean deleted = movieFile.delete(id);  // Chama o método de exclusão no arquivo
                        if (deleted) {
                            System.out.println("Successfully deleted movie.");
                        } else {
                            System.out.println("Error deleting movie");
                        }
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                } else {
                    System.out.println("Movie not found.");
                }
            } catch (Exception e) {
                System.out.println("System error. Unable to delete movie!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid ID");
        }
    }

    public void listAllMovies() {
    System.out.println("\nAll movies:");

    try {
        // Obtém a lista de filmes
        List<Movie> movies = movieFile.readAll();

        if (!movies.isEmpty()) { // Verifica se a lista tem elementos
            for (Movie movie : movies) {
                showMovie(movie);
            }
        } else {
            System.out.println("No movies found.");
        }
    } catch (Exception e) {
        System.out.println("System error. Unable to list movies!");
        e.printStackTrace();
    }
}

    public void orderAllMovies() {
        System.out.println("\nOrdering all movies...");

        try {
            ExternalSort sorter = new ExternalSort(movieFile); // Criar instância
            sorter.sort(); // Chamar o método da instância
            System.out.println("Movies successfully ordered!");
        } catch (Exception e) {
            System.out.println("System error. Unable to order movies!");
            e.printStackTrace();
        }
    }


    public void showMovie(Movie movie) {
        if (movie != null) {
            System.out.println("\nMovie details:");
            System.out.println("----------------------");
            System.out.printf("Type......: %s%n", movie.getType());
            System.out.printf("Title.....: %s%n", movie.getTitle());
            System.out.printf("Director..: %s%n", movie.getDirector());
            
            // Exibir elenco
            String[] cast = movie.getCast();
            if (cast.length > 0) {
                System.out.print("Cast......: ");
                for (int i = 0; i < cast.length; i++) {
                    System.out.print(cast[i].trim());
                    if (i < cast.length - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            } else {
                System.out.println("Cast......: Not specified");
            }
            
            System.out.printf("Country...: %s%n", movie.getCountry());
            System.out.printf("Date Added: %s%n", movie.getDateAdded());
            System.out.printf("Release Year: %d%n", movie.getReleaseYear());
            System.out.printf("Rating....: %s%n", movie.getRating());
            System.out.printf("Duration..: %s%n", movie.getDuration());
            System.out.printf("Listed In.: %s%n", movie.getListedIn());
            System.out.printf("Description: %s%n", movie.getDescription());
            
            System.out.println("----------------------");
        }
    }
}