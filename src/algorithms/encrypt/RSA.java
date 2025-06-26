package algorithms.encrypt;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

public class RSA {
    private BigInteger n, d, e;
    private int bitlen = 1024; // Tamanho da chave em bits

    public RSA() {
        generateKeys();
    }

    private void generateKeys() {
        SecureRandom r = new SecureRandom();
        BigInteger p = new BigInteger(bitlen / 2, 100, r);
        BigInteger q = new BigInteger(bitlen / 2, 100, r);
        n = p.multiply(q);
        
        BigInteger m = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        e = new BigInteger("65537"); // Valor comum para e
        while (m.gcd(e).intValue() > 1) {
            e = e.add(new BigInteger("2"));
        }
        d = e.modInverse(m);
    }

    public String encrypt(String message) {
        if (message == null || message.isEmpty()) {
            return message; // Retorna nulo ou vazio sem criptografar
        }
        
        try {
            byte[] bytes = message.getBytes();
            if (bytes.length == 0) {
                return "";
            }
            BigInteger messageBigInt = new BigInteger(bytes);
            BigInteger encrypted = messageBigInt.modPow(e, n);
            return Base64.getEncoder().encodeToString(encrypted.toByteArray());
        } catch (Exception ex) {
            System.err.println("Erro ao criptografar: " + ex.getMessage());
            return message; // Retorna o original em caso de erro
        }
    }

    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return encrypted; // Retorna nulo ou vazio sem descriptografar
        }
        
        try {
            byte[] bytes = Base64.getDecoder().decode(encrypted);
            if (bytes.length == 0) {
                return "";
            }
            BigInteger encryptedBigInt = new BigInteger(bytes);
            BigInteger decrypted = encryptedBigInt.modPow(d, n);
            return new String(decrypted.toByteArray());
        } catch (Exception ex) {
            System.err.println("Erro ao descriptografar: " + ex.getMessage());
            return encrypted; // Retorna o original em caso de erro
        }
    }

    public String getPublicKey() {
        return "e=" + e.toString() + ",n=" + n.toString();
    }

    public String getPrivateKey() {
        return "d=" + d.toString() + ",n=" + n.toString();
    }
}