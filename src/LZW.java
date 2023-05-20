import java.io.*;
import java.util.*;

public class LZW {
    private static final int TAM_DIC = 4096;
    private static final int MAX_TAM_COD = 12;

    public static void comprimir(String inputFilePath, String outputFilePath) {
        try (InputStream inputStream = new FileInputStream(inputFilePath);
             DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFilePath))) {

            // Construir o dicionário inicial com os caracteres ASCII
            Dicionario dicionario = new Dicionario(TAM_DIC);

            int nextCode = TAM_DIC + 1;
            int currentByte = inputStream.read();
            StringBuilder currentSequence = new StringBuilder();
            currentSequence.append((char) currentByte);

            while (currentByte != -1) {
                currentByte = inputStream.read();

                if (currentByte != -1) {
                    currentSequence.append((char) currentByte);

                    if (!dicionario.containsSequence(currentSequence.toString())) {
                        // Gravar a codificação da sequência anterior no arquivo de saída
                        outputStream.writeShort(dicionario.getCode(currentSequence.substring(0, currentSequence.length() - 1)));

                        // Adicionar a nova sequência ao dicionário
                        if (nextCode < (1 << MAX_TAM_COD)) {
                            dicionario.addSequence(currentSequence.toString(), nextCode++);
                        }

                        // Reiniciar a sequência de entrada
                        currentSequence = new StringBuilder();
                        currentSequence.append((char) currentByte);
                    }
                }
            }

            // Gravar a codificação final da sequência de entrada atual no arquivo de saída
            outputStream.writeShort(dicionario.getCode(currentSequence.toString()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void descomprimir(String inputFilePath, String outputFilePath) {
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(inputFilePath));
             OutputStream outputStream = new FileOutputStream(outputFilePath)) {
    
            // Construir o dicionário inicial com os caracteres ASCII
            Dicionario dicionario = new Dicionario(TAM_DIC);
    
            int nextCode = TAM_DIC + 1;
    
            int currentCode = inputStream.readShort();
            StringBuilder outputSequence = new StringBuilder(dicionario.getSequence(currentCode));
    
            while (true && currentCode != -1) {
                int previousCode = currentCode;
                currentCode = inputStream.readShort();
    
                if (currentCode == -1) {
                    break;
                }
    
                String currentSequence;
                if (dicionario.containsCode(currentCode)) {
                    currentSequence = dicionario.getSequence(currentCode);
                } else if (currentCode == nextCode) {
                    currentSequence = outputSequence.toString() + outputSequence.charAt(0);
                } else {
                    throw new IllegalStateException("Invalid compressed data");
                }
    
                outputSequence.append(currentSequence);
    
                // Gravar apenas o último caractere da sequência no arquivo de saída
                outputStream.write(outputSequence.charAt(outputSequence.length() - 1));
    
                // Adicionar a nova sequência ao dicionário
                if (nextCode < (1 << MAX_TAM_COD)) {
                    dicionario.addSequence(outputSequence.toString(), nextCode++);
                }
    
                outputSequence = new StringBuilder(currentSequence);
            }
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Dicionario {
        private final Map<String, Integer> sequenceToCode;
        private final List<String> codeToSequence;

        public Dicionario(int size) {
            this.sequenceToCode = new HashMap<>();
            this.codeToSequence = new ArrayList<>(size);

            // Inicializar o dicionário com os caracteres ASCII
            for (int i = 0; i < size; i++) {
                String sequence = String.valueOf((char) i);
                sequenceToCode.put(sequence, i);
                codeToSequence.add(sequence);
            }
        }

        public void addSequence(String sequence, int code) {
            sequenceToCode.put(sequence, code);
            codeToSequence.add(sequence);
        }

        public boolean containsSequence(String sequence) {
            return sequenceToCode.containsKey(sequence);
        }

        public boolean containsCode(int code) {
            return code >= 0 && code < codeToSequence.size();
        }

        public int getCode(String sequence) {
            return sequenceToCode.get(sequence);
        }

        public String getSequence(int code) {
            return codeToSequence.get(code);
        }
    }

    public static void main(String[] args) {
        // Exemplo de uso para comprimir um arquivo
        String inputFile = "gamees.bin";
        String comprimiredFile = "compressed.bin";
        comprimir(inputFile, comprimiredFile);

        // Exemplo de uso para descomprimir um arquivo
        String descomprimiredFile = "descompressed.bin";
        descomprimir(comprimiredFile, descomprimiredFile);
    }
}