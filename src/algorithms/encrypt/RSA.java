package algorithms.encrypt;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class RSA {
    private BigInteger n, d, e;
    private int bitlen = 1024; // Tamanho da chave em bits
    
    // O tamanho máximo do bloco de dados em bytes.
    // Para uma chave de 1024 bits (128 bytes), um valor seguro é 117.
    private static final int MAX_CHUNK_SIZE = 117; 

    public RSA() {
        generateKeys();
    }

    private void generateKeys() {
        // ... seu código de generateKeys() permanece o mesmo
        SecureRandom r = new SecureRandom();
        BigInteger p = new BigInteger(bitlen / 2, 100, r);
        BigInteger q = new BigInteger(bitlen / 2, 100, r);
        n = p.multiply(q);
        BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        e = new BigInteger("65537");
        while (m.gcd(e).intValue() > 1) {
            e = e.add(new BigInteger("2"));
        }
        d = e.modInverse(m);
    }
    
    // --- MÉTODOS ORIGINAIS (para mensagens curtas) ---
    // Você pode mantê-los ou torná-los privados

    public String encrypt(String message) {
        if (message == null || message.isEmpty()) return message;
        
        // Delega para o novo método que lida com blocos
        return encryptInChunks(message);
    }

    public String decrypt(String encryptedMessage) {
        if (encryptedMessage == null || encryptedMessage.isEmpty()) return encryptedMessage;
        
        // Delega para o novo método que lida com blocos
        return decryptInChunks(encryptedMessage);
    }
    
    // --- NOVOS MÉTODOS PARA CRIPTOGRAFIA EM BLOCOS ---

    private String encryptInChunks(String message) {
        try {
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            List<String> encryptedChunks = new ArrayList<>();
            
            for (int i = 0; i < messageBytes.length; i += MAX_CHUNK_SIZE) {
                int end = Math.min(messageBytes.length, i + MAX_CHUNK_SIZE);
                byte[] chunk = new byte[end - i];
                System.arraycopy(messageBytes, i, chunk, 0, chunk.length);

                // Criptografa o chunk
                BigInteger messageBigInt = new BigInteger(chunk);
                BigInteger encrypted = messageBigInt.modPow(e, n);
                
                // Converte para Base64 para evitar problemas com caracteres
                encryptedChunks.add(Base64.getEncoder().encodeToString(encrypted.toByteArray()));
            }
            
            // Junta os chunks criptografados com um separador
            return String.join(";", encryptedChunks);

        } catch (Exception ex) {
            System.err.println("Erro ao criptografar em blocos: " + ex.getMessage());
            return message; // Retorna o original em caso de erro
        }
    }

    private String decryptInChunks(String encryptedData) {
        try {
            String[] encryptedChunks = encryptedData.split(";");
            StringBuilder decryptedMessage = new StringBuilder();
            
            for (String chunk : encryptedChunks) {
                if(chunk.trim().isEmpty()) continue;
                
                // Decodifica de Base64
                byte[] bytes = Base64.getDecoder().decode(chunk);
                BigInteger encryptedBigInt = new BigInteger(bytes);
                
                // Descriptografa o chunk
                BigInteger decrypted = encryptedBigInt.modPow(d, n);
                
                // Adiciona o texto descriptografado ao resultado final
                decryptedMessage.append(new String(decrypted.toByteArray(), StandardCharsets.UTF_8));
            }
            
            return decryptedMessage.toString();

        } catch (Exception ex) {
            System.err.println("Erro ao descriptografar em blocos: " + ex.getMessage());
            return encryptedData; // Retorna o original em caso de erro
        }
    }

    public String getPublicKey() {
        return "e=" + e.toString() + ",n=" + n.toString();
    }

    public String getPrivateKey() {
        return "d=" + d.toString() + ",n=" + n.toString();
    }
}