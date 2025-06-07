package algorithms.compression;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

class HuffmanNode implements Comparable<HuffmanNode> {
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

public class Huffman {

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

        if (no.b != 0) {
            codigos.put(no.b, codigo);
        }

        constroiCodigos(no.esquerdo, codigo + "0", codigos);
        constroiCodigos(no.direito, codigo + "1", codigos);
    }

    // Versão buscando na tabela de códigos.
    public static byte[] decodifica(String bits, HashMap<Byte, String> codigos) {
        // cria o dicionário invertido bits->byte
        HashMap<String, Byte> inverso = new HashMap<>();
        for (Map.Entry<Byte, String> entry : codigos.entrySet()) {
            inverso.put(entry.getValue(), entry.getKey());
        }

        List<Byte> lista = new ArrayList<>();
        String temp = "";

        for (int i = 0; i < bits.length(); i++) {
            temp += bits.charAt(i);
            if (inverso.containsKey(temp)) {
                lista.add(inverso.get(temp));
                temp = "";
            }
        }
        // converte lista para array de bytes
        byte[] resultado = new byte[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            resultado[i] = lista.get(i);
        }
        return resultado;
    }
}
