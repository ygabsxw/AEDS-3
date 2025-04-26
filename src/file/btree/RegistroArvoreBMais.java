package file.btree;

import java.io.IOException;

public interface RegistroArvoreBMais<T> {

  public short size(); // tamanho FIXO do registro

  public byte[] toByteArray() throws IOException; // representação do elemento em um vetor de bytes

  public void fromByteArray(byte[] ba) throws IOException; // vetor de bytes a ser usado na construção do elemento

  public int compareTo(T obj); // compara dois elementos

  public T clone(); // clonagem de objetos

}
