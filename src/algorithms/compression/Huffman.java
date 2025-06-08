package algorithms.compression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Huffman {
    public static class HuffmanNode implements Comparable<HuffmanNode> {
        byte b;
        int frequencia;
        HuffmanNode esquerdo, direito;

        public HuffmanNode(byte b, int f) {
            this.b = b;
            this.frequencia = f;
            esquerdo = direito = null;
        }

        @Override
        public int compareTo(HuffmanNode o) {
            return this.frequencia - o.frequencia;
        }
    }

    public static HashMap<Byte, String> codifica(byte[] sequencia) {
        HashMap<Byte, Integer> mapaDeFrequencias = new HashMap<>();
        for (byte c : sequencia) {
            mapaDeFrequencias.put(c, mapaDeFrequencias.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Byte b : mapaDeFrequencias.keySet()) {
            pq.add(new HuffmanNode(b, mapaDeFrequencias.get(b)));
        }

        while (pq.size() > 1) {
            HuffmanNode esquerdo = pq.poll();
            HuffmanNode direito = pq.poll();

            HuffmanNode pai = new HuffmanNode((byte)0, esquerdo.frequencia + direito.frequencia);
            pai.esquerdo = esquerdo;
            pai.direito = direito;

            pq.add(pai);
        }

        HuffmanNode raiz = pq.poll();
        HashMap<Byte, String> codigos = new HashMap<>();
        constroiCodigos(raiz, "", codigos);

        return codigos;
    }

    private static void constroiCodigos(HuffmanNode no, String codigo, HashMap<Byte, String> codigos) {
        if (no == null) {
            return;
        }

        if (no.esquerdo == null && no.direito == null) {
            codigos.put(no.b, codigo);
        }

        constroiCodigos(no.esquerdo, codigo + "0", codigos);
        constroiCodigos(no.direito, codigo + "1", codigos);
    }

    // Versão buscando na tabela de códigos.
    public static byte[] decodifica(String bits, HuffmanNode raiz) {
        List<Byte> lista = new ArrayList<>();
        HuffmanNode atual = raiz;

        for (int i = 0; i < bits.length(); i++) {
            char c = bits.charAt(i);
            atual = (c == '0') ? atual.esquerdo : atual.direito;

            if (atual.esquerdo == null && atual.direito == null) {
                lista.add(atual.b);
                atual = raiz;
            }
        }

        byte[] resultado = new byte[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            resultado[i] = lista.get(i);
        }
        return resultado;
    }

    public static HuffmanNode reconstruirArvore(HashMap<Byte, String> codigos) {
        HuffmanNode raiz = new HuffmanNode((byte)0, 0);
        for (Map.Entry<Byte, String> entry : codigos.entrySet()) {
            byte b = entry.getKey();
            String caminho = entry.getValue();
            HuffmanNode atual = raiz;
            for (char c : caminho.toCharArray()) {
                if (c == '0') {
                    if (atual.esquerdo == null)
                        atual.esquerdo = new HuffmanNode((byte)0, 0);
                    atual = atual.esquerdo;
                } else {
                    if (atual.direito == null)
                        atual.direito = new HuffmanNode((byte)0, 0);
                    atual = atual.direito;
                }
            }
            atual.b = b;
        }
        return raiz;
    }
}
