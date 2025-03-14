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
        int numberOfBlocks = (int) Math.ceil((double) fileSize / blockSize);
    
        if (numberOfBlocks <= 0) {
            throw new IllegalArgumentException("Invalid block size calculation: " + numberOfBlocks);
        }
    
        RandomAccessFile raf = new RandomAccessFile(file, "r");
    
        for (int i = 0; i < numberOfBlocks; i++) {
            List<Movie> block = new ArrayList<>();
    
            while (raf.getFilePointer() < fileSize && block.size() < blockSize) {
                long pos = raf.getFilePointer();
    
                if (pos + 3 > fileSize) { // Verifica se há espaço para lápide e tamanho
                    break;
                }
    
                byte lapide = raf.readByte();
                short size = raf.readShort();
    
                if (lapide != ' ') {
                    raf.seek(pos + 3 + size); // Pula para o próximo registro
                    continue;
                }
    
                if (pos + 3 + size > fileSize) { // Evita estouro de leitura
                    break;
                }
    
                byte[] bytes = new byte[size];
                raf.readFully(bytes);
    
                try {
                    Movie movie = new Movie();
                    movie.fromByteArray(bytes); 
                    block.add(movie);
                } catch (Exception e) {
                    break; // Evita continuar lendo registros inválidos
                }
            }
    
            if (block.isEmpty()) {
                System.out.println("Bloco " + i + " está vazio.");
            } else {
                try {
                    saveSortedBlock(block, i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    
        raf.close();
    }
    
    

    // Salva um bloco ordenado em um arquivo temporário
    private static void saveSortedBlock(List<Movie> block, int index) throws Exception {
        File tempDir = new File("temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs(); // Cria o diretório se não existir
        }
    
        File tempFile = new File("temp/run_" + index + ".db");
        RandomAccessFile runFile = new RandomAccessFile(tempFile, "rw");
    
        // System.out.println("Saving block to: " + tempFile.getAbsolutePath());
        for (Movie movie : block) {
            byte[] bytes = movie.toByteArray(); // Converte o filme em bytes
            if (bytes == null || bytes.length == 0) {
                System.out.println("Warning: Trying to write empty movie data!");
                continue;
            }
            
            runFile.writeByte(' '); // Lapide
            runFile.writeShort(bytes.length);
            runFile.write(bytes);
            
            System.out.println("Written movie ID: " + movie.getId() + ", Size: " + bytes.length);
        }
        runFile.close();
    }

    // Intercala os arquivos ordenados usando Heap e cria o arquivo final ordenado
    public static void mergeSortedBlocks(int numBlocks, String finalFile) throws Exception {
        System.out.println("entrou aqui mergeSort");
        PriorityQueue<HeapHelper> heap = new PriorityQueue<>(Comparator.comparingInt(e -> e.movie.getId())); // Ordena pela ID
        RandomAccessFile[] tempFiles = new RandomAccessFile[numBlocks]; // Arquivos temporários
    
        for (int i = 0; i < numBlocks; i++) {
            File file = new File("temp/run_" + i + ".db");
            if (!file.exists() || file.length() == 0) {
                System.out.println("Warning: File temp/run_" + i + ".db does not exist or is empty.");
                continue; // Evita tentar abrir arquivos inexistentes ou vazios
            }
        
            tempFiles[i] = new RandomAccessFile(file, "r"); // Inicializa o arquivo
        
        
            if (tempFiles[i].getFilePointer() < tempFiles[i].length()) {
                Movie movie = readNextMovie(tempFiles[i]);
                if (movie != null) {
                    heap.add(new HeapHelper(movie, i)); // Adiciona o primeiro filme do arquivo na heap
                }
            }
        }
    
        RandomAccessFile sortedFile = new RandomAccessFile(finalFile, "rw");
    
        while (!heap.isEmpty()) {
            HeapHelper element = heap.poll(); // Remove o menor filme da heap
            byte[] bytes = element.movie.toByteArray(); // Converte o filme para bytes
            sortedFile.writeByte(' '); // Lápide
            sortedFile.writeShort(bytes.length); // Tamanho do filme
            sortedFile.write(bytes); // Escreve o filme no arquivo final
            if (tempFiles[element.origin].getFilePointer() < tempFiles[element.origin].length()) {
                Movie nextMovie = readNextMovie(tempFiles[element.origin]);
                if (nextMovie != null) {
                    heap.add(new HeapHelper(nextMovie, element.origin)); // Adiciona o próximo filme do mesmo arquivo na heap
                }
            }
        }
    
        for (RandomAccessFile tempFile : tempFiles) {
            if (tempFile != null) {
                tempFile.close();
            }
        }
        sortedFile.close();
    }

    // Lê o próximo filme de um arquivo temporário
    private static Movie readNextMovie(RandomAccessFile file) throws Exception {
        if (file.getFilePointer() < file.length()) {
            long pos = file.getFilePointer();
            byte lapide = file.readByte();
            short size = file.readShort();
            byte[] bytes = new byte[size];
            file.read(bytes);
    
            if (lapide == ' ') {
                Movie movie = new Movie();
                movie.fromByteArray(bytes);
                return movie;
            } else {
                System.out.println("Skipping deleted record");
            }
        }
        return null;
    }
}