package file;

import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.List;
import model.*;

public class FileManager<T extends Movie> {
    final int TAM_CABECALHO = 12;
    RandomAccessFile arquivo;
    String nomeArquivo;
    Constructor<T> construtor;

    public FileManager(String na, Constructor<T> c) throws Exception {
        java.io.File d = new java.io.File(".\\dados");
        if (!d.exists()) d.mkdir();

        d = new java.io.File(".\\dados\\" + na);
        if (!d.exists()) d.mkdir();

        this.nomeArquivo = na + ".db";

        // Criação do arquivo caso não exista
        java.io.File file = new java.io.File(this.nomeArquivo);
        if (!file.exists()) {
            file.createNewFile();  // Cria o arquivo vazio para evitar erro de FileNotFoundException
            System.out.println("Arquivo criado: " + this.nomeArquivo);
        }

        this.construtor = c;
        arquivo = new RandomAccessFile(this.nomeArquivo, "rw");

        if (arquivo.length() < TAM_CABECALHO) {
            arquivo.writeInt(0);   // Último ID
            arquivo.writeLong(-1); // Lista de registros excluídos
            System.out.println("Inicializando o arquivo com os dados do CSV...");
            initializeFromCSV("dataset/netflix_titles_modified.csv");
        }
    }

    private void initializeFromCSV(String csvFilePath) throws Exception {
        System.out.println("Lendo dados do CSV...");
        readCSV csvReader = new readCSV(csvFilePath);
        List<Movie> movies = csvReader.read();

        for (Movie movie : movies) {
            T obj = construtor.newInstance();
            obj.setId(movie.getId());
            obj.setType(movie.getType());
            obj.setTitle(movie.getTitle());
            obj.setDirector(movie.getDirector());
            obj.setCast(movie.getCast());
            obj.setCountry(movie.getCountry());
            obj.setDateAdded(movie.getDateAdded());
            obj.setReleaseYear(movie.getReleaseYear());
            obj.setRating(movie.getRating());
            obj.setDuration(movie.getDuration());
            obj.setListedIn(movie.getListedIn());
            obj.setDescription(movie.getDescription());
            this.create(obj);
        }
    }

    public int create(T obj) throws Exception {
        arquivo.seek(0);
        int proximoID = arquivo.readInt() + 1;
        arquivo.seek(0);
        arquivo.writeInt(proximoID);
        obj.setId(proximoID);
        byte[] b = obj.toByteArray();

        arquivo.seek(arquivo.length());
        arquivo.writeByte(' ');      // Lápide
        arquivo.writeShort(b.length);  // Tamanho do vetor de bytes
        arquivo.write(b);              // Vetor de bytes

        return obj.getId();
    }

    public T read(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            byte lapide = arquivo.readByte();
            short tam = arquivo.readShort();
            byte[] b = new byte[tam];
            arquivo.read(b);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(b);
                if (obj.getId() == id) {
                    return obj;
                }
            }
        }
        return null;
    }

    public boolean delete(int id) throws Exception {
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long pos = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tam = arquivo.readShort();
            byte[] b = new byte[tam];
            arquivo.read(b);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(b);
                if (obj.getId() == id) {
                    arquivo.seek(pos);
                    arquivo.writeByte('*'); // Marca como excluído
                    return true;
                }
            }
        }
        return false;
    }

    public boolean update(T novoObj) throws Exception {
        boolean updated = false;
        arquivo.seek(TAM_CABECALHO);
        while (arquivo.getFilePointer() < arquivo.length()) {
            long pos = arquivo.getFilePointer();
            byte lapide = arquivo.readByte();
            short tam = arquivo.readShort();
            byte[] b = new byte[tam];
            arquivo.read(b);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(b);
                if (obj.getId() == novoObj.getId()) {
                    byte[] novoBytes = novoObj.toByteArray();
                    short novoTam = (short) novoBytes.length;

                    if (novoTam <= tam) {
                        arquivo.seek(pos + 3);
                        arquivo.write(novoBytes);
                    } else {
                        arquivo.seek(pos);
                        arquivo.writeByte('*');
                        arquivo.seek(arquivo.length()); // vai para o final
                        arquivo.getFilePointer();
                        arquivo.writeByte(' '); // lapide
                        arquivo.writeShort(novoTam); // tamanho
                        arquivo.write(novoBytes); // escreve o novo
                        System.out.println("Registro movido para o final: ID " + novoObj.getId());
                    }
                    updated = true;
                    break;
                }
            }
        }
        return updated;
    }

    public int getNextId() throws Exception {
        arquivo.seek(0);
        return arquivo.readInt() + 1;
    }

    public void close() throws Exception {
        arquivo.close();
    }
}
