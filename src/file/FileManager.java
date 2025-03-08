package file;

import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.List;

import model.*;

public class FileManager<T extends Movie> {  // Renomeado para FileManager
    final int TAM_CABECALHO = 12;
    RandomAccessFile arquivo;
    String nomeArquivo;
    Constructor<T> construtor;
    HashExtensivel<ParIDEndereco> indiceDireto;

    public FileManager(String na, Constructor<T> c) throws Exception {  // Alterado para FileManager
        java.io.File d = new java.io.File(".\\dados");  // Usando java.io.File corretamente
        if(!d.exists())
            d.mkdir();

        d = new java.io.File(".\\dados\\"+na);
        if(!d.exists())
            d.mkdir();

        this.nomeArquivo = ".\\dados\\"+na+"\\"+na+".db";
        this.construtor = c;

        // Inicializar o índice direto antes de abrir o arquivo
        indiceDireto = new HashExtensivel<>(ParIDEndereco.class.getConstructor(), 4, ".\\dados\\"+na+"\\"+na+".d.db", ".\\dados\\"+na+"\\"+na+".c.db");

        arquivo = new RandomAccessFile(this.nomeArquivo, "rw");
        if(arquivo.length() < TAM_CABECALHO) {
            // Inicializa o arquivo, criando seu cabeçalho
            arquivo.writeInt(0);   // último ID
            arquivo.writeLong(-1);   // lista de registros marcados para exclusão 

            // Chama o método para ler e inserir dados do CSV
            System.out.println("Inicializando o arquivo com os dados do CSV...");
            initializeFromCSV("dataset/netflix_titles_modified.csv");
        }

         // Adicionando a depuração para o índice direto
        System.out.println("Inicializando o índice direto...");
        if (indiceDireto != null) {
            System.out.println("Índice direto inicializado com sucesso.");
        } else {
            System.out.println("Falha ao inicializar o índice direto.");
        }
    }

    // Método que lê os dados do CSV e cria os objetos
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
        arquivo.seek(0);
        int proximoID = arquivo.readInt() + 1;
        arquivo.seek(0);
        arquivo.writeInt(proximoID);
        System.out.println("Novo ID: " + proximoID); // Debugging
        obj.setId(proximoID);
        byte[] b = obj.toByteArray();

        long endereco = getDeleted(b.length);   // tenta reusar algum espaço de registro excluído
        if(endereco == -1) {   // nenhum espaço disponível; escreve o registro no fim do arquivo  
            arquivo.seek(arquivo.length());
            endereco = arquivo.getFilePointer();
            arquivo.writeByte(' ');      // lápide
            arquivo.writeShort(b.length);  // tamanho do vetor de bytes
            arquivo.write(b);              // vetor de bytes
        } else {
            arquivo.seek(endereco);
            arquivo.writeByte(' ');      // limpa o lápide
            arquivo.skipBytes(2);        // pula o indicador de tamanho para preservá-lo
            arquivo.write(b);              // vetor de bytes
        }

        indiceDireto.create(new ParIDEndereco(proximoID, endereco));
        
        return obj.getId();
    }
    
    public T read(int id) throws Exception {
        T obj;
        short tam;
        byte[] b;
        byte lapide;

        ParIDEndereco pid = indiceDireto.read(id);
        if(pid != null) {
            arquivo.seek(pid.getEndereco());
            obj = construtor.newInstance();
            lapide = arquivo.readByte();
            if(lapide == ' ') {
                tam = arquivo.readShort();
                b = new byte[tam];
                arquivo.read(b);
                obj.fromByteArray(b);
                if(obj.getId() == id)
                    return obj;
            }
        }
        return null;
    }

    public boolean delete(int id) throws Exception {
        T obj;
        short tam;
        byte[] b;
        byte lapide;

        ParIDEndereco pie = indiceDireto.read(id);
        if(pie != null) {
            arquivo.seek(pie.getEndereco());
            obj = construtor.newInstance();
            lapide = arquivo.readByte();
            if(lapide == ' ') {
                tam = arquivo.readShort();
                b = new byte[tam];
                arquivo.read(b);
                obj.fromByteArray(b);
                if(obj.getId() == id) {
                    if(indiceDireto.delete(id)) {
                        arquivo.seek(pie.getEndereco());
                        arquivo.write('*');
                        addDeleted(tam, pie.getEndereco());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean update(T novoObj) throws Exception {
        T obj;
        short tam;
        byte[] b;
        byte lapide;
        ParIDEndereco pie = indiceDireto.read(novoObj.getId());
        if(pie != null) {
            arquivo.seek(pie.getEndereco());
            obj = construtor.newInstance();
            lapide = arquivo.readByte();
            if(lapide == ' ') {
                tam = arquivo.readShort();
                b = new byte[tam];
                arquivo.read(b);
                obj.fromByteArray(b);
                if(obj.getId() == novoObj.getId()) {

                    byte[] b2 = novoObj.toByteArray();
                    short tam2 = (short) b2.length;

                    // sobrescreve o registro
                    if(tam2 <= tam) {
                        arquivo.seek(pie.getEndereco() + 3);
                        arquivo.write(b2);
                    }

                    // move o novo registro para o fim
                    else {
                        // exclui o registro anterior
                        arquivo.seek(pie.getEndereco());
                        arquivo.write('*');
                        addDeleted(tam, pie.getEndereco());                        

                        // grava o novo registro
                        long novoEndereco = getDeleted(b.length);   // tenta reusar algum espaço de registro excluído
                        if(novoEndereco == -1) {   // nenhum espaço disponível; escreve o registro no fim do arquivo  
                            arquivo.seek(arquivo.length());
                            novoEndereco = arquivo.getFilePointer();
                            arquivo.writeByte(' ');       // lápide
                            arquivo.writeShort(tam2);       // tamanho do vetor de bytes
                            arquivo.write(b2);              // vetor de bytes
                        } else {
                            arquivo.seek(novoEndereco);
                            arquivo.writeByte(' ');       // limpa o lápide
                            arquivo.skipBytes(2);         // pula o indicador de tamanho para preservá-lo
                            arquivo.write(b2);              // vetor de bytes
                        }

                        // atualiza o índice direto
                        indiceDireto.update(new ParIDEndereco(novoObj.getId(), novoEndereco));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // adiciona um registro à lista de excluídos (espaços disponíveis para reuso)
    public void addDeleted(int tamanhoEspaco, long enderecoEspaco) throws Exception {
        long anterior = 4; // início da lista
        arquivo.seek(anterior);
        long endereco = arquivo.readLong(); // endereço do elemento que será testado
        long proximo = -1; // endereço do elemento seguinte da lista
        int tamanho;
        if(endereco == -1) {  // lista vazia
            arquivo.seek(4);
            arquivo.writeLong(enderecoEspaco);
            arquivo.seek(enderecoEspaco + 3);
            arquivo.writeLong(-1);
        } else {
            do {
                arquivo.seek(endereco + 1);
                tamanho = arquivo.readShort();
                proximo = arquivo.readLong();
                if(tamanho > tamanhoEspaco) {  // encontrou a posição de inserção (antes do elemento atual)
                    if(anterior == 4) // será o primeiro elemento da lista
                        arquivo.seek(anterior);
                    else
                        arquivo.seek(anterior + 3);
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(endereco);
                    break;
                }
                if(proximo == -1) {  // fim da lista
                    arquivo.seek(endereco + 3);
                    arquivo.writeLong(enderecoEspaco);
                    arquivo.seek(enderecoEspaco + 3);
                    arquivo.writeLong(+1);
                    break;
                }
                anterior = endereco;
                endereco = proximo;
            } while (endereco != -1);
        }
    }
    
    // retira um registro à lista de excluídos para reuso, mas com o risco de algum desperdício
    // se necessário, o código pode ser alterado para controlar um limite máximo de desperdício
    public long getDeleted(int tamanhoNecessario) throws Exception {
        long anterior = 4; // início da lista
        arquivo.seek(anterior);
        long endereco = arquivo.readLong(); // endereço do elemento que será testado
        long proximo = -1; // endereço do elemento seguinte da lista
        int tamanho;
        while(endereco != -1) {
            arquivo.seek(endereco + 1);
            tamanho = arquivo.readShort();
            proximo = arquivo.readLong();
            if(tamanho > tamanhoNecessario) {  
                if(anterior == 4)  // o elemento é o primeiro da lista 
                    arquivo.seek(anterior);
                else
                    arquivo.seek(anterior + 3);
                arquivo.writeLong(proximo);
                break;
            }
            anterior = endereco;
            endereco = proximo;
        }
        return endereco;
    }

    public int getNextId() throws Exception {
        arquivo.seek(0);
        return arquivo.readInt() + 1;
    }

    public void close() throws Exception {
        arquivo.close();
        indiceDireto.close();
    }
}
