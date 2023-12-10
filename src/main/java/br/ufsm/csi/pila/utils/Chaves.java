package br.ufsm.csi.pila.utils;

import java.io.*;
import java.security.*;

public class Chaves {

    private static final String CHAVE_PRIVADA_FILE = "privateKey.der";
    private static final String CHAVE_PUBLICA_FILE = "publicKey.der";

    public static void main(String[] args) {
        try {
            // Inicializa o gerador de chaves RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024); // Tamanho da chave: 1024 bits

            // Gera o par de chaves
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Obtém a chave privada e pública
            PrivateKey chavePrivada = keyPair.getPrivate();
            PublicKey chavePublica = keyPair.getPublic();

            // Exibe as chaves
            System.out.println("Chave Privada: " + chavePrivada);
            System.out.println("Chave Pública: " + chavePublica);

            // Salva as chaves em arquivos
            salvarChaveEmArquivo(CHAVE_PRIVADA_FILE, chavePrivada);
            salvarChaveEmArquivo(CHAVE_PUBLICA_FILE, chavePublica);

            // Carrega as chaves de volta dos arquivos (apenas para fins de demonstração)
            PrivateKey chavePrivadaCarregada = carregarChavePrivadaDeArquivo(CHAVE_PRIVADA_FILE);
            PublicKey chavePublicaCarregada = carregarChavePublicaDeArquivo(CHAVE_PUBLICA_FILE);

            // Exibe as chaves carregadas
            System.out.println("Chave Privada Carregada: " + chavePrivadaCarregada);
            System.out.println("Chave Pública Carregada: " + chavePublicaCarregada);

        } catch (NoSuchAlgorithmException | IOException | ClassNotFoundException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }

    private static void salvarChaveEmArquivo(String fileName, Key chave) throws IOException {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(fileName))) {
            outputStream.writeObject(chave);
        }
    }

    public static PrivateKey carregarChavePrivadaDeArquivo(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            return (PrivateKey) inputStream.readObject();
        }
    }

    public static PublicKey carregarChavePublicaDeArquivo(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName))) {
            return (PublicKey) inputStream.readObject();
        }
    }
}
