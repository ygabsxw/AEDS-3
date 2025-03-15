package database.algorithms.externalOrdering;

import file.FileManager;
import model.Movie;
import java.io.*;
import java.util.*;

public class ExternalSort {
    // Definição da constante que determina o número máximo de registros a serem carregados em memória.
    private static final int MAX_RECORDS_IN_MEMORY = 1000;
    private FileManager<Movie> fileManager;

    // Construtor que recebe um FileManager para realizar as operações de leitura e gravação.
    public ExternalSort(FileManager<Movie> fileManager) {
        this.fileManager = fileManager;
    }

    // Método principal para executar a ordenação externa.
    public void sort() throws Exception {
        // Criação das "corridas" iniciais (arquivos temporários) e ordenação inicial dos dados.
        List<String> runFiles = createInitialRuns();
        // Mescla os arquivos ordenados gerados nas corridas iniciais.
        mergeRuns(runFiles);
    }

    // Cria os arquivos temporários com dados ordenados para cada "corrida" (run).
    private List<String> createInitialRuns() throws Exception {
        List<Movie> buffer = new ArrayList<>();  // Buffer temporário para armazenar filmes antes de escrever no arquivo.
        List<String> runFiles = new ArrayList<>(); // Lista de arquivos temporários criados.
        int runCount = 0; // Contador de corridas (arquivos temporários).

        // Lê todos os filmes do arquivo de dados utilizando o fileManager.
        for (Movie movie : fileManager.readAll()) {
            buffer.add(movie); // Adiciona o filme ao buffer.
            if (buffer.size() >= MAX_RECORDS_IN_MEMORY) { // Se o buffer atingir o limite máximo de registros na memória.
                // Cria um arquivo temporário e escreve os dados no arquivo.
                runFiles.add(writeRun(buffer, runCount++));
                buffer.clear(); // Limpa o buffer para a próxima corrida.
            }
        }

        // Se o buffer não estiver vazio, escreve o restante dos filmes.
        if (!buffer.isEmpty()) {
            runFiles.add(writeRun(buffer, runCount));
        }

        return runFiles; // Retorna a lista de arquivos temporários criados.
    }

    // Método que escreve um arquivo temporário ordenado.
    private String writeRun(List<Movie> buffer, int runIndex) throws IOException {
        // Ordena os filmes no buffer pelo ID.
        buffer.sort(Comparator.comparing(Movie::getId));
        String fileName = "src/database/data/run_" + runIndex + ".db"; // Define o nome do arquivo temporário.

        // Escreve os filmes no arquivo temporário utilizando ObjectOutputStream.
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            for (Movie movie : buffer) {
                oos.writeObject(movie); // Grava cada filme no arquivo.
            }
        }
        return fileName; // Retorna o nome do arquivo temporário criado.
    }

    // Método que mescla os arquivos temporários ordenados.
    private void mergeRuns(List<String> runFiles) throws Exception {
        // PriorityQueue garante que os filmes serão retirados na ordem correta durante a fusão (por ID).
        PriorityQueue<MovieEntry> pq = new PriorityQueue<>(Comparator.comparing(MovieEntry::getMovieId));
        Map<ObjectInputStream, String> streams = new HashMap<>(); // Mapeia os streams de entrada para os nomes dos arquivos.

        // Abre todos os arquivos temporários para leitura.
        for (String runFile : runFiles) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(runFile));
            streams.put(ois, runFile); // Armazena o stream de entrada e o nome do arquivo.
            addToQueue(ois, pq); // Adiciona o primeiro filme de cada arquivo à fila de prioridade.
        }

        // Limpa o arquivo original (no caso, o arquivo de filmes) antes de reescrever.
        fileManager.clear();

        // Enquanto a fila de prioridade não estiver vazia, continue retirando filmes ordenados e escrevendo no arquivo final.
        while (!pq.isEmpty()) {
            MovieEntry entry = pq.poll(); // Remove o filme de menor ID da fila de prioridade.
            Movie movie = entry.movie; // Obtém o filme.

            // Grava o filme no arquivo final (preservando o ID original).
            fileManager.createAfterOrder(movie);

            // Adiciona o próximo filme da mesma entrada de arquivo, caso haja mais filmes.
            addToQueue(entry.stream, pq);
        }

        // Fecha todos os streams de leitura e exclui os arquivos temporários criados.
        for (ObjectInputStream ois : streams.keySet()) {
            ois.close();
            new File(streams.get(ois)).delete(); // Remove arquivos temporários.
        }
    }

    // Adiciona um filme da stream à fila de prioridade.
    private void addToQueue(ObjectInputStream ois, PriorityQueue<MovieEntry> pq) throws IOException {
        try {
            Movie movie = (Movie) ois.readObject(); // Lê um filme do arquivo.
            pq.offer(new MovieEntry(movie, ois)); // Adiciona o filme à fila de prioridade.
        } catch (EOFException | ClassNotFoundException ignored) {
            // Ignora exceções caso o fim do arquivo seja alcançado ou a classe não seja encontrada.
        }
    }

    // Classe auxiliar que contém um filme e o stream de onde ele foi lido.
    private static class MovieEntry {
        Movie movie;  // O filme
        ObjectInputStream stream; // O stream de onde o filme foi lido.

        MovieEntry(Movie movie, ObjectInputStream stream) {
            this.movie = movie;
            this.stream = stream;
        }

        // Obtém o ID do filme, usado para ordenar os filmes na fila de prioridade.
        int getMovieId() {
            return movie.getId();
        }
    }
}