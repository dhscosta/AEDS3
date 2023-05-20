import java.io.*;
import java.util.*;

public class LZW {
    //tamanhos de dicionario e de cada codigo do dicionario
    private static final int TAM_DIC = 4096;
    private static final int MAX_TAM_COD = 12;

    //funçao de compressão
    public static void comprimir(String inputFilePath, String outputFilePath) {
        try (
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFilePath));
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilePath)))
        ) {
            Dicionario dicionario = new Dicionario(TAM_DIC);          //inicializa dicionario
            int nextCode = 1;
            int currentByte = inputStream.read();                     //lê os bytes do arquivo
            StringBuilder currentSequence = new StringBuilder();      
            currentSequence.append((char) currentByte);              //biulda uma string colocando os dados sempre em sequencia

            while (currentByte != -1) {
                //tratamento para EOF
                currentByte = inputStream.read();
                if (currentByte != -1) {
                    currentSequence.append((char) currentByte);   //adiciona os dados na sequencia

                    if (!dicionario.containsSequence(currentSequence.toString())) {
                        outputStream.writeShort(dicionario.getCode(currentSequence.substring(0, currentSequence.length() - 1)));  //grava a sequencia em bytes

                        if (nextCode < (1 << MAX_TAM_COD)) {
                            dicionario.addSequence(currentSequence.toString(), nextCode++);  //adiciona a sequencia no dicionario (se houver espaço)
                        }
                        //reinicia a sequencia
                        currentSequence.setLength(0);
                        currentSequence.append((char) currentByte);
                    }
                }
            }

            outputStream.writeShort(dicionario.getCode(currentSequence.toString()));  //grava a sqeuencia em bytes
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //função de descompressão 
    public static void descomprimir(String inputFilePath, String outputFilePath) {
        try (
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFilePath)));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFilePath))
        ) {
            Dicionario dicionario = new Dicionario(TAM_DIC);        //inicializa o dicionário
            int nextCode = TAM_DIC + 1;
            int currentCode;

            //tratamento para EOF
            if (inputStream.available() >= 2) {
                currentCode = inputStream.readShort();
            } else {
                currentCode = -1;
            }
            StringBuilder outputSequence = new StringBuilder(dicionario.getSequence(currentCode)); //biulda pegando as sequencias do dicionario

            while (currentCode != -1) {
                //tratamento de EOF
                if (inputStream.available() >= 2) {
                    currentCode = inputStream.readShort();
                } else {
                    currentCode = -1;
                }

                if (currentCode == -1) {
                    break;
                }

                String currentSequence;
                if (dicionario.containsCode(currentCode)) {                 //se a sequencia lida ja existir pega do dicionario
                    currentSequence = dicionario.getSequence(currentCode);
                } else if (currentCode == nextCode) {                       
                    currentSequence = outputSequence.toString() + outputSequence.charAt(0);
                } else {
                    throw new IllegalStateException("Invalid compressed data");
                }

                outputSequence.append(currentSequence);
                outputStream.write(outputSequence.charAt(outputSequence.length() - 1));

                if (nextCode < (1 << MAX_TAM_COD)) {
                    dicionario.addSequence(outputSequence.toString(), nextCode++);
                }

                outputSequence.setLength(currentSequence.length());
                outputSequence.replace(0, currentSequence.length(), currentSequence);
            }

            outputStream.flush();
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

            //inicializa sequencia com todos os caracteres da tabela ASCII
            for (int i = 0; i < size; i++) {
                String sequence = String.valueOf((char) i);
                sequenceToCode.put(sequence, i);
                codeToSequence.add(sequence);
            }
        }
        //adiciona nova sequencia no dicionario
        public void addSequence(String sequence, int code) {
            sequenceToCode.put(sequence, code);
            codeToSequence.add(sequence);
        }
        //retorna se a sequencia existe
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
}