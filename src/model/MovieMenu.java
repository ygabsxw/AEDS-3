package model;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import algorithms.searchPattern.BoyerMoore;
import algorithms.searchPattern.KMP;
import algorithms.compression.Huffman;
import algorithms.compression.LZW;
import algorithms.compression.VetorDeBits;
import algorithms.encrypt.Cifra;
import algorithms.encrypt.RSA;
import algorithms.externalOrdering.ExternalSort;
import file.*;
import file.hash.FileManagerHash;
import file.btree.FileManagerArvoreB;
import file.inverted.FileManagerListaInvertida;


public class MovieMenu {
    private final RSA rsa = new RSA();
    FileManager<Movie> movieFile;
    private static Scanner sc = new Scanner(System.in);
    public static String encryptionType = "";

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
            System.out.println(" 8 - Compression");
            System.out.println(" 9 - Decompression");
            System.out.println(" 10 - Pattern Matching");
            System.out.println(" 11 - Encrypt Cifra");
            System.out.println(" 12 - Encrypt RSA - ONLY FOR SEQUENTIAL FILE");
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
                case 8:
                    compress();
                    break;
                case 9:
                    decompress();
                    break;
                case 10:
                    searchPattern();
                    break;
                case 11:
                    encryptAllMoviesCifra();
                    break;
                 case 12:
                    encryptAllMoviesRSA();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Invalid option!");
                    return;
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

    public void compress() {
        try {
            String inputPath = movieFile.getFilePath();  // Agora é dinâmico!
            String outputDir = "src/database/data/compression/";
            new File(outputDir).mkdirs(); // Garante que o diretório existe

            String outputLZW = outputDir + "movies_compressed_lzw.db";
            String outputHuff = outputDir + "movies_compressed_huffman.db";

            // Leitura do arquivo original
            File inputFile = new File(inputPath);
            byte[] originalData = java.nio.file.Files.readAllBytes(inputFile.toPath());
            int originalSize = originalData.length;

            System.out.println("\n--- COMPACTAÇÃO ---");
            System.out.println("Original file: " + inputPath);
            System.out.println("Original size: " + originalSize + " bytes");

            // Compressão LZW
            long startLZW = System.nanoTime();
            byte[] compressedLZW = LZW.codifica(originalData);
            long endLZW = System.nanoTime();
            long timeLZW = endLZW - startLZW;
            java.nio.file.Files.write(java.nio.file.Paths.get(outputLZW), compressedLZW);
            int sizeLZW = compressedLZW.length;

            // Compressão Huffman
            long startHuff = System.nanoTime();
            HashMap<Byte, String> codigos = Huffman.codifica(originalData);
            StringBuilder encoded = new StringBuilder();
            for (byte b : originalData)
                encoded.append(codigos.get(b));
            VetorDeBits vb = new VetorDeBits();
            for (int i = 0; i < encoded.length(); i++)
                if (encoded.charAt(i) == '1') vb.set(i);
            byte[] compressedHuff = vb.toByteArray();
            long endHuff = System.nanoTime();
            long timeHuff = endHuff - startHuff;

            // Salva tamanho dos bits válidos, dicionário e dados comprimidos
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputHuff))) {
                oos.writeInt(encoded.length());   // <--- Salva o tamanho total de bits válidos
                oos.writeObject(codigos);
                oos.writeObject(compressedHuff);
            }
            int sizeHuff = compressedHuff.length;

            // Resultados
            System.out.println("\n--- RESULTS ---");
            double percLZW = ((double) (originalSize - sizeLZW) / originalSize) * 100;
            double percHuff = ((double) (originalSize - sizeHuff) / originalSize) * 100;

            System.out.printf("LZW: %d bytes | %.2f%% compression | %.2f ms%n", sizeLZW, percLZW, timeLZW / 1e6);
            System.out.printf("Huffman: %d bytes | %.2f%% compression | %.2f ms%n", sizeHuff, percHuff, timeHuff / 1e6);

            if (percLZW > percHuff)
                System.out.println("LZW had better compression.");
            else if (percHuff > percLZW)
                System.out.println("Huffman had better compression.");
            else
                System.out.println("Both had equal compression.");

            if (timeLZW < timeHuff)
                System.out.println("LZW was faster");
            else if (timeHuff < timeLZW)
                System.out.println("Huffman was faster");
            else
                System.out.println("Both algorithms took the same amount of time.");

        } catch (Exception e) {
            System.err.println("Erro na compressão: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void decompress() {
        try {
            Scanner sc = new Scanner(System.in);
            String inputPath = movieFile.getFilePath();
            String compressedDir = "src/database/data/compression/";

            System.out.println("\n--- DECOMPRESSION ---");
            System.out.println(" 1 - Decompress with LZW");
            System.out.println(" 2 - Decompress with Huffman");
            System.out.println(" 0 - Back");
            System.out.print("Choose the algorithm: ");
            int option = Integer.parseInt(sc.nextLine());

            byte[] decompressedData = null;
            long startTime, endTime;
            double timeLZW = -1, timeHuffman = -1;

            switch (option) {
                case 1:
                    // LZW
                    try {
                        String compressedLZWPath = compressedDir + "movies_compressed_lzw.db";
                        byte[] compressedLZW = Files.readAllBytes(Paths.get(compressedLZWPath));

                        startTime = System.nanoTime();
                        decompressedData = LZW.decodifica(compressedLZW);
                        endTime = System.nanoTime();

                        timeLZW = (endTime - startTime) / 1e6;
                        Files.write(Paths.get(inputPath), decompressedData);

                        System.out.printf("LZW decompressed successfully! Time: %.2f ms%n", timeLZW);
                    } catch (Exception e) {
                        System.err.println("Error decompressing with LZW: " + e.getMessage());
                    }
                    break;

                case 2:
                    // Huffman
                    try {
                        String compressedHuffPath = compressedDir + "movies_compressed_huffman.db";
                        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(compressedHuffPath));
                        int totalBits = ois.readInt();  // total de bits válidos
                        @SuppressWarnings("unchecked")
                        HashMap<Byte, String> codigos = (HashMap<Byte, String>) ois.readObject();
                        byte[] compressedBytes = (byte[]) ois.readObject();
                        ois.close();

                        // Constrói a sequência de bits e corta só os bits válidos
                        StringBuilder sequenciaCodificada = VetorDeBits.fromByteArray(compressedBytes);
                        String bitsValidos = sequenciaCodificada.substring(0, totalBits);

                        // Usa árvore reconstruída
                        Huffman.HuffmanNode raiz = Huffman.reconstruirArvore(codigos);
                        startTime = System.nanoTime();
                        decompressedData = Huffman.decodifica(bitsValidos, raiz);
                        endTime = System.nanoTime();

                        timeHuffman = (endTime - startTime) / 1e6;

                        // Depuração: imprime os primeiros bytes descomprimidos
                        System.out.println("Bytes descomprimidos (primeiros 100):");
                        for (int i = 0; i < Math.min(decompressedData.length, 100); i++) {
                            System.out.print((char) decompressedData[i]);
                        }
                        System.out.println();

                        Files.write(Paths.get(inputPath), decompressedData);

                        System.out.printf("Huffman decompressed successfully! Time: %.2f ms%n", timeHuffman);
                    } catch (Exception e) {
                        System.err.println("Error decompressing with Huffman: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case 0:
                    break;
                default:
                    System.out.println("Invalid option!");
                    return;
            }

        } catch (Exception e) {
            System.err.println("Error decompressing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void searchPattern() {
        try {
            Scanner sc = new Scanner(System.in);
            String inputPath = movieFile.getFilePath();

            System.out.println("\n--- PATTERN MATCH ---");
            System.out.println(" 1 - Search with KMP");
            System.out.println(" 2 - Search with Boyer-Moore");
            System.out.println(" 0 - Back");
            System.out.print("Choose the algorithm: ");
            int option = Integer.parseInt(sc.nextLine());

            System.out.print("Enter pattern to search: ");
            String pattern = sc.nextLine();

            byte[] fileBytes = Files.readAllBytes(Paths.get(inputPath));
            String content = new String(fileBytes, StandardCharsets.ISO_8859_1);
            
            int index = -1;
            long startTime, endTime;
            double elapsedTime = 0;

            switch (option) {
                case 1:
                    try {
                        startTime = System.nanoTime();
                        KMP kmp = new KMP();
                        index = kmp.search(content, pattern);
                        endTime = System.nanoTime();
                        elapsedTime = (endTime - startTime) / 1e6;

                        System.out.println((index != -1) ?
                            "Pattern found at position: " + index :
                            "Pattern not found.");
                        System.out.printf("KMP Time: %.2f ms%n", elapsedTime);
                    } catch (Exception e) {
                        System.err.println("Error using KMP: " + e.getMessage());
                    }
                    break;

                case 2:
                    try {
                        startTime = System.nanoTime();
                        BoyerMoore bm = new BoyerMoore(pattern);
                        index = bm.search(content);
                        endTime = System.nanoTime();
                        elapsedTime = (endTime - startTime) / 1e6;

                        System.out.println((index != -1) ?
                            "Pattern found at position: " + index :
                            "Pattern not found.");
                        System.out.printf("Boyer-Moore Time: %.2f ms%n", elapsedTime);
                    } catch (Exception e) {
                        System.err.println("Error using Boyer-Moore: " + e.getMessage());
                    }
                    break;

                case 0:
                    return;

                default:
                    System.out.println("Invalid option!");
                    return;
            }

        } catch (Exception e) {
            System.err.println("Error in pattern search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void encryptAllMoviesCifra() {
        int chaveCesar = 3;

        try {
            List<Movie> todos = movieFile.readAll();

            for (Movie movie : todos) {
                
                movie.setTitle(Cifra.cifrar(movie.getTitle(), chaveCesar));
                movie.setType(Cifra.cifrar(movie.getType(), chaveCesar));
                movie.setDirector(Cifra.cifrar(movie.getDirector(), chaveCesar));
                String[] elenco = movie.getCast();
                for (int i = 0; i < elenco.length; i++) {
                    elenco[i] = Cifra.cifrar(elenco[i], chaveCesar);
                }
                movie.setCast(elenco);
                movie.setCountry(Cifra.cifrar(movie.getCountry(), chaveCesar));
                movie.setRating(Cifra.cifrar(movie.getRating(), chaveCesar));
                movie.setDuration(Cifra.cifrar(movie.getDuration(), chaveCesar));
                movie.setListedIn(Cifra.cifrar(movie.getListedIn(), chaveCesar));
                movie.setDescription(Cifra.cifrar(movie.getDescription(), chaveCesar));

                movieFile.update(movie); // Atualiza no arquivo com dados criptografados
            }

            MovieMenu.encryptionType = "cesar";
            
            System.out.println("Todos os filmes foram criptografados com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao criptografar todos os registros:");
            e.printStackTrace();
        }
    }

    public void encryptAllMoviesRSA() {
        try {
            List<Movie> todos = movieFile.readAll();

            for (Movie movie : todos) {
                // Criptografa cada campo de texto apenas se não for nulo/vazio
                movie.setTitle(handleEncryption(movie.getTitle()));
                movie.setType(handleEncryption(movie.getType()));
                movie.setDirector(handleEncryption(movie.getDirector()));
                
                String[] elenco = movie.getCast();
                for (int i = 0; i < elenco.length; i++) {
                    elenco[i] = handleEncryption(elenco[i]);
                }
                movie.setCast(elenco);
                
                movie.setCountry(handleEncryption(movie.getCountry()));
                movie.setRating(handleEncryption(movie.getRating()));
                movie.setDuration(handleEncryption(movie.getDuration()));
                movie.setListedIn(handleEncryption(movie.getListedIn()));
                movie.setDescription(handleEncryption(movie.getDescription()));

                movieFile.update(movie);
            }

            MovieMenu.encryptionType = "rsa";

            System.out.println("Todos os filmes foram criptografados com RSA com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao criptografar todos os registros com RSA:");
            e.printStackTrace();
        }
    }

    // Método auxiliar para lidar com valores nulos/vazios
    private String handleEncryption(String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        return rsa.encrypt(value);
    }


    public void showMovie(Movie movie) {
        if (movie == null) return;

        int chaveCesar = 3;

        if (MovieMenu.encryptionType.equalsIgnoreCase("cesar")) {
            movie.setType(Cifra.decifrar(movie.getType(), chaveCesar));
            movie.setTitle(Cifra.decifrar(movie.getTitle(), chaveCesar));
            movie.setDirector(Cifra.decifrar(movie.getDirector(), chaveCesar));
            String[] cast = movie.getCast();
            for (int i = 0; i < cast.length; i++) {
                cast[i] = Cifra.decifrar(cast[i], chaveCesar);
            }
            movie.setCast(cast);
            movie.setCountry(Cifra.decifrar(movie.getCountry(), chaveCesar));
            movie.setRating(Cifra.decifrar(movie.getRating(), chaveCesar));
            movie.setDuration(Cifra.decifrar(movie.getDuration(), chaveCesar));
            movie.setListedIn(Cifra.decifrar(movie.getListedIn(), chaveCesar));
            movie.setDescription(Cifra.decifrar(movie.getDescription(), chaveCesar));

        } else if (MovieMenu.encryptionType.equalsIgnoreCase("rsa")) {
            movie.setType(rsa.decrypt(movie.getType()));
            movie.setTitle(rsa.decrypt(movie.getTitle()));
            movie.setDirector(rsa.decrypt(movie.getDirector()));
            String[] cast = movie.getCast();
            for (int i = 0; i < cast.length; i++) {
                cast[i] = rsa.decrypt(cast[i]);
            }
            movie.setCast(cast);
            movie.setCountry(rsa.decrypt(movie.getCountry()));
            movie.setRating(rsa.decrypt(movie.getRating()));
            movie.setDuration(rsa.decrypt(movie.getDuration()));
            movie.setListedIn(rsa.decrypt(movie.getListedIn()));
            movie.setDescription(rsa.decrypt(movie.getDescription()));
        }

        // Exibe o filme
        System.out.println("\nMovie details:");
        System.out.println("----------------------");
        System.out.printf("Type......: %s%n", movie.getType());
        System.out.printf("Title.....: %s%n", movie.getTitle());
        System.out.printf("Director..: %s%n", movie.getDirector());
        String[] cast = movie.getCast();
        if (cast.length > 0) {
            System.out.print("Cast......: ");
            for (int i = 0; i < cast.length; i++) {
                System.out.print(cast[i].trim());
                if (i < cast.length - 1) System.out.print(", ");
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