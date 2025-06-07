package file.btree;

import java.io.RandomAccessFile;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import file.FileManager;
import file.readCSV;
import model.Movie;

public class FileManagerArvoreB<T extends Movie> implements FileManager<T> {

    private RandomAccessFile dados;
    private Constructor<T> construtor;
    private ArvoreBMais<ParIntInt> indicePrimario;
    private String nomeArquivo;

    public FileManagerArvoreB(String nomeBase, Constructor<T> c) throws Exception {
        this.construtor = c;
        
        // Definir caminhos
        String baseDir = "src/database/data/btree/";
        this.nomeArquivo = baseDir + nomeBase + ".db";
        String indicePrimarioPath = baseDir + nomeBase + ".b.db";
        
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Inicializar arquivos
        dados = new RandomAccessFile(this.nomeArquivo, "rw");
        indicePrimario = new ArvoreBMais<>(ParIntInt.class.getConstructor(), 4, indicePrimarioPath);

        if (dados.length() < 12) {
            dados.writeInt(0);   // último ID
            dados.writeLong(-1); // lista de excluídos
            System.out.println("Inicializando o arquivo com os dados do CSV...");
            initializeFromCSV("dataset/netflix_titles_modified.csv");
        }
    }

    private void initializeFromCSV(String csvFilePath) throws Exception {
        System.out.println("Lendo CSV para popular dados e índice...");
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
        dados.seek(0);
        int currentID = dados.readInt();

        int id = obj.getId();
        if (id <= 0) {
            id = currentID + 1;
            obj.setId(id);
        } else if (id > currentID) {
            currentID = id;
        }

        dados.seek(0);
        dados.writeInt(currentID);

        dados.seek(dados.length());
        long pos = dados.getFilePointer();
        dados.writeByte(' ');
        byte[] ba = obj.toByteArray();
        dados.writeShort(ba.length);
        dados.write(ba);

        // Agora insere no índice primário: (id -> posição no arquivo)
        ParIntInt par = new ParIntInt(id, (int) pos);
        System.out.println("Inserindo no índice: " + par);
        indicePrimario.create(par);
        
        return obj.getId();
    }

    public T read(int id) throws Exception {
        // Agora usa o índice para buscar!
        List<ParIntInt> resultados = indicePrimario.read(new ParIntInt(id, -1));
        if (resultados.size() > 0) {
            int posicao = resultados.get(0).getNum2();
            dados.seek(posicao);
            byte lapide = dados.readByte();
            short tam = dados.readShort();
            byte[] ba = new byte[tam];
            dados.readFully(ba);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(ba);
                return obj;
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
            dados.readFully(ba);
            if (lapide == ' ') {
                T obj = construtor.newInstance();
                obj.fromByteArray(ba);
                lista.add(obj);
            }
        }
        return lista;
    }

    public boolean update(T novo) throws Exception {
        // Localiza a posição pelo índice
        List<ParIntInt> resultados = indicePrimario.read(new ParIntInt(novo.getId(), -1));
        if (resultados.size() > 0) {
            int posicao = resultados.get(0).getNum2();
            dados.seek(posicao);
            byte lapide = dados.readByte();
            short tam = dados.readShort();
    
            byte[] novoBa = novo.toByteArray();
            
            if (lapide == ' ') {
                if (novoBa.length <= tam) {
                    // Cabe no espaço original: sobrescreve
                    dados.write(novoBa);
                } else {
                    // Não cabe: marca como excluído e grava no final
                    dados.seek(posicao);
                    dados.writeByte('*');
    
                    dados.seek(dados.length());
                    int novaPosicao = (int) dados.getFilePointer();
                    dados.writeByte(' ');
                    dados.writeShort(novoBa.length);
                    dados.write(novoBa);
    
                    // Atualiza o índice para a nova posição
                    indicePrimario.update(new ParIntInt(novo.getId(), novaPosicao));
                }
                return true;
            }
        }
        return false;
    }

    public boolean delete(int id) throws Exception {
        // Localiza a posição pelo índice
        List<ParIntInt> resultados = indicePrimario.read(new ParIntInt(id, -1));
        if (resultados.size() > 0) {
            int posicao = resultados.get(0).getNum2();
            dados.seek(posicao);
            dados.writeByte('*'); // marca como excluído

            // Remove do índice primário
            indicePrimario.delete(new ParIntInt(id, -1));
            return true;
        }
        return false;
    }

    int createAfterOrder(T obj) throws Exception {
        return 0;
    }

    public void clear() throws Exception { }

    public void close() throws Exception {
        dados.close();
    }

    public int getNextId() throws Exception {
        dados.seek(0);
        return dados.readInt() + 1;
    }

    public List<T> searchByType(String type) throws Exception {
        return null; // lista invertida
    }

    public String getFilePath() {
        return this.nomeArquivo;
    }
}