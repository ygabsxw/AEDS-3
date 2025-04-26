package file.btree;

import java.io.RandomAccessFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import file.FileManager;
import file.readCSV;
import model.Movie;

public class FileManagerArvoreB<T extends Movie> implements FileManager<T> {

    private RandomAccessFile dados;
    private Constructor<T> construtor;
    private ArvoreBMais<ParIntInt> indiceSecundario;
    private String nomeArquivo;

    public FileManagerArvoreB(String nomeBase, Constructor<T> c) throws Exception {
        this.construtor = c;
        this.nomeArquivo = "src/database/data/" + nomeBase + ".db";
        dados = new RandomAccessFile(this.nomeArquivo, "rw");

        indiceSecundario = new ArvoreBMais<>(ParIntInt.class.getConstructor(), 4, "src/database/data/" + nomeBase + ".b.db");

        if (dados.length() < 12) {
            dados.writeInt(0);   // último ID
            dados.writeLong(-1); // lista de excluídos
            System.out.println("Inicializando o arquivo com os dados do CSV...");
            initializeFromCSV("dataset/netflix_titles_modified.csv");
        }
    }

    private void initializeFromCSV(String csvFilePath) throws Exception {
        System.out.println("Entrou na função...");
        readCSV csvReader = new readCSV(csvFilePath);
        List<Movie> movies = csvReader.read();  // Lê os dados do CSV
    
        // Insere cada filme no arquivo, criando o objeto T diretamente
        for (Movie movie : movies) {
            // Criar um novo objeto T a partir do construtor genérico
            T obj = construtor.newInstance();
            // Preencher o objeto T com os dados do CSV
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
            
            System.out.println("Objeto Criado...");
    
            this.create(obj);  // Insere o objeto T no banco de dados
        }
    }

    public int create(T obj) throws Exception {
        dados.seek(0);
        int id = dados.readInt() + 1;
        dados.seek(0);
        dados.writeInt(id);
        obj.setId(id);

        dados.seek(dados.length());
        long pos = dados.getFilePointer();
        dados.writeByte(' ');
        byte[] ba = obj.toByteArray();
        dados.writeShort(ba.length);
        dados.write(ba);

        // Indexar por ano de lançamento
        if (obj.getReleaseYear() != null)
            indiceSecundario.create(new ParIntInt(obj.getReleaseYear(), id));

        return id;
    }

    public T read(int id) throws Exception {
        dados.seek(12);
        while (dados.getFilePointer() < dados.length()) {
            long pos = dados.getFilePointer();
            byte lapide = dados.readByte();
            short tam = dados.readShort();
            byte[] ba = new byte[tam];
            dados.read(ba);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(ba);
                if (obj.getId() == id) return obj;
            }
        }
        return null;
    }

    public List<T> readAll() throws Exception {
        List<T> lista = new ArrayList<>();
        dados.seek(12);
        while (dados.getFilePointer() < dados.length()) {
            byte lapide = dados.readByte();
            short tam = dados.readShort();
            byte[] ba = new byte[tam];
            dados.read(ba);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(ba);
                lista.add(obj);
            }
        }
        return lista;
    }

    public boolean update(T novo) throws Exception {
        // Localizar e excluir o registro antigo
        dados.seek(12);
        while (dados.getFilePointer() < dados.length()) {
            long pos = dados.getFilePointer();
            byte lapide = dados.readByte();
            short tam = dados.readShort();
            byte[] ba = new byte[tam];
            dados.read(ba);
    
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(ba);
                if (obj.getId() == novo.getId()) {
                    // Marca como excluído
                    dados.seek(pos);
                    dados.writeByte('*');
                    
                    // Não precisa remover do índice, pois o ID será regravado igual
                    break;
                }
            }
        }
    
        // Agora grava o novo registro (com mesmo ID!)
        dados.seek(dados.length());
        long novaPosicao = dados.getFilePointer();
        dados.writeByte(' ');
        byte[] novoBa = novo.toByteArray();
        dados.writeShort(novoBa.length);
        dados.write(novoBa);
    
        // Atualiza índice secundário, se necessário (aqui depende se você indexa por campo alterado)
        // Exemplo: atualiza ano de lançamento se mudar
        indiceSecundario.create(new ParIntInt(novo.getReleaseYear(), novo.getId()));
    
        return true;
    }

    public boolean delete(int id) throws Exception {
        dados.seek(12);
        while (dados.getFilePointer() < dados.length()) {
            long pos = dados.getFilePointer();
            byte lapide = dados.readByte();
            short tam = dados.readShort();
            byte[] ba = new byte[tam];
            dados.read(ba);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(ba);
                if (obj.getId() == id) {
                    dados.seek(pos);
                    dados.writeByte('*');
                    return true;
                }
            }
        }
        return false;
    }

    int createAfterOrder(T obj) throws IOException {
        return 0;
    }

    public void clear() throws Exception {
        // dados.setLength(0);
        // dados.writeInt(0);   // Reset last ID
        // dados.writeLong(-1); // Reset deleted list
        // indiceSecundario.clear(); // Clear secondary index
    }

    public void close() throws Exception {
        dados.close();
    }

    public int getNextId() throws Exception {
        dados.seek(0);
        return dados.readInt() + 1;
    }
}