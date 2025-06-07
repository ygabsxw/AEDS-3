package file.inverted;

import model.Movie;
import file.FileManager;
import file.readCSV;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class FileManagerListaInvertida<T extends Movie> implements FileManager<T> {

    private RandomAccessFile dados;
    private Constructor<T> construtor;
    private ListaInvertida indiceSecundario;
    private String nomeArquivo;

    public FileManagerListaInvertida(String nomeBase, Constructor<T> c) throws Exception {
        this.construtor = c;
        
        // Definir caminho base
        String baseDir = "src/database/data/inverted/";
        this.nomeArquivo = baseDir + nomeBase + ".db";
        
        // Criar diretórios se não existirem
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs(); // Cria todos os diretórios necessários na hierarquia
        }
        
        // Inicializar arquivos
        dados = new RandomAccessFile(this.nomeArquivo, "rw");
        
        // Definir caminhos para os arquivos de índice
        String indiceDadosPath = baseDir + nomeBase + ".i.d.db";
        String indiceListaPath = baseDir + nomeBase + ".i.l.db";
        
        indiceSecundario = new ListaInvertida(5, // quantidade de IDs por bloco
            indiceDadosPath,
            indiceListaPath);

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

        String tipo = obj.getType().trim().toLowerCase();
        if (!tipo.isEmpty()) {
            indiceSecundario.create(tipo, new ElementoLista(id, 1));
        }

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
        // Localiza, exclui e remove do índice
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
                    dados.seek(pos);
                    dados.writeByte('*');

                    // Remove do índice antigo
                    String[] antigas = obj.getListedIn().split(",");
                    for (String cat : antigas) {
                        cat = cat.trim();
                        if (!cat.isEmpty())
                            indiceSecundario.delete(cat, obj.getId());
                    }
                    break;
                }
            }
        }

        // Grava novo registro
        dados.seek(dados.length());
        dados.writeByte(' ');
        byte[] novoBa = novo.toByteArray();
        dados.writeShort(novoBa.length);
        dados.write(novoBa);

        // Reinsere no índice
        String[] categorias = novo.getListedIn().split(",");
        for (String cat : categorias) {
            cat = cat.trim();
            if (!cat.isEmpty())
                indiceSecundario.create(cat, new ElementoLista(novo.getId(), 1));
        }

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
                    
                    // Remove do índice
                    String[] categorias = obj.getListedIn().split(",");
                    for (String cat : categorias) {
                        cat = cat.trim();
                        if (!cat.isEmpty())
                            indiceSecundario.delete(cat, id);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    int createAfterOrder(T obj) throws IOException {
        return 0;
    }

    public void clear() throws IOException {
        // arquivo.setLength(0);
        // arquivo.seek(0);
        // arquivo.writeInt(0);  // Reseta ID
        // arquivo.writeLong(-1); // Reseta os deletados
    }

    public int getNextId() throws Exception {
        dados.seek(0);
        return dados.readInt() + 1;
    }

    public void close() throws Exception {
        dados.close();
    }

    public List<T> searchByType(String type) throws Exception {
        List<T> encontrados = new ArrayList<>();
        ElementoLista[] elementos = indiceSecundario.read(type.trim().toLowerCase());
        for (ElementoLista e : elementos) {
            T obj = read(e.getId());
            if (obj != null) encontrados.add(obj);
        }
        return encontrados;
    }

    public String getFilePath() {
        return this.nomeArquivo;
    }
}