package database.algorithms.externalOrdering;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

import model.Movie;

public class ExternalSort {

    // Divide o arquivo em blocos menores, ordena cada bloco e salva em arquivos temporários
    public static void splitFileIntoBlocks(String fileName, int blockSize) throws IOException {
        File file = new File(fileName);
        if (!file.exists() || file.length() == 0) {
            throw new IOException("Error: The movie file is empty or does not exist.");
        }

        long fileSize = file.length();
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        int blockIndex = 0;

        while (raf.getFilePointer() < fileSize) {
            List<Movie> block = new ArrayList<>();

            while (raf.getFilePointer() < fileSize && block.size() < blockSize) {
                long pos = raf.getFilePointer();
                if (pos + 3 > fileSize) break; // Evita ler um cabeçalho incompleto

                byte lapide = raf.readByte();
                short size = raf.readShort();

                if (lapide != ' ') {
                    raf.seek(pos + 3 + size);
                    continue;
                }

                if (pos + 3 + size > fileSize) break; // Evita estouro de leitura

                byte[] bytes = new byte[size];
                raf.readFully(bytes);

                try {
                    Movie movie = new Movie();
                    movie.fromByteArray(bytes);
                    block.add(movie);
                } catch (Exception e) {
                    // Evita continuar lendo registros inválidos
                }
            }

            if (!block.isEmpty()) {
                try {
                    saveSortedBlock(block, blockIndex);
                    blockIndex++;
                } catch (Exception e) {
                    e.printStackTrace();  // Exibe erro no console sem interromper o programa
                }
            }
        }

        raf.close();
    }

    // Salva um bloco ordenado em um arquivo temporário
    private static void saveSortedBlock(List<Movie> block, int index) throws Exception {
        File tempDir = new File("temp");
        if (!tempDir.exists()) tempDir.mkdirs();

        File tempFile = new File("temp/run_" + index + ".db");
        RandomAccessFile runFile = new RandomAccessFile(tempFile, "rw");

        block.sort(Comparator.comparingInt(Movie::getId));

        for (Movie movie : block) {
            byte[] bytes = movie.toByteArray();
            if (bytes == null || bytes.length == 0) continue;

            runFile.writeByte(' '); // Lápide
            runFile.writeShort(bytes.length);
            runFile.write(bytes);
        }
        runFile.close();
    }

    // Intercala os arquivos ordenados usando Heap e cria o arquivo final ordenado
    public static void mergeSortedBlocks(int numBlocks, String finalFile) throws Exception {
        PriorityQueue<HeapHelper> heap = new PriorityQueue<>(Comparator.comparingInt(e -> e.movie.getId()));
        RandomAccessFile[] tempFiles = new RandomAccessFile[numBlocks];

        for (int i = 0; i < numBlocks; i++) {
            File file = new File("temp/run_" + i + ".db");
            if (!file.exists() || file.length() == 0) continue;

            tempFiles[i] = new RandomAccessFile(file, "r");
            Movie movie = readNextMovie(tempFiles[i]);
            if (movie != null) {
                heap.add(new HeapHelper(movie, i));
            }
        }

        RandomAccessFile sortedFile = new RandomAccessFile(finalFile, "rw");

        while (!heap.isEmpty()) {
            HeapHelper element = heap.poll();
            byte[] bytes = element.movie.toByteArray();
            sortedFile.writeByte(' '); // Lápide
            sortedFile.writeShort(bytes.length);
            sortedFile.write(bytes);

            if (tempFiles[element.origin].getFilePointer() < tempFiles[element.origin].length()) {
                Movie nextMovie = readNextMovie(tempFiles[element.origin]);
                if (nextMovie != null) {
                    heap.add(new HeapHelper(nextMovie, element.origin));
                }
            }
        }

        // Fecha e remove os arquivos temporários após a fusão
        for (int i = 0; i < numBlocks; i++) {
            if (tempFiles[i] != null) {
                tempFiles[i].close();
                new File("temp/run_" + i + ".db").delete();
            }
        }

        sortedFile.close();
    }

    // Lê o próximo filme de um arquivo temporário
    private static Movie readNextMovie(RandomAccessFile file) throws Exception {
        while (file.getFilePointer() < file.length()) {
            long pos = file.getFilePointer();
            byte lapide = file.readByte();
            short size = file.readShort();
            if (lapide != ' ') {
                file.seek(pos + 3 + size);
                continue;
            }

            byte[] bytes = new byte[size];
            file.readFully(bytes);
            Movie movie = new Movie();
            movie.fromByteArray(bytes);
            return movie;
        }
        return null;
    }
}