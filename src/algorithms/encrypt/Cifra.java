package algorithms.encrypt;

public class Cifra {
    public static String cifrar(String texto, int chave) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            sb.append((char)(c + chave));
        }
        return sb.toString();
    }

    public static String decifrar(String texto, int chave) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            sb.append((char)(c - chave));
        }
        return sb.toString();
    }
}