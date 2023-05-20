import java.io.*;
import java.util.*;

public class LZW {
    private static final int TAM_DIC = 4096;
    private static final int MAX_TAM_COD = 12;

    public static void comprimir(String inputFilePath, String outputFilePath) {
        try (
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFilePath));
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilePath)))
        ) {
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
                        outputStream.writeShort(dicionario.getCode(currentSequence.substring(0, currentSequence.length() - 1)));

                        if (nextCode < (1 << MAX_TAM_COD)) {
                            dicionario.addSequence(currentSequence.toString(), nextCode++);
                        }

                        currentSequence.setLength(0);
                        currentSequence.append((char) currentByte);
                    }
                }
            }

            outputStream.writeShort(dicionario.getCode(currentSequence.toString()));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void descomprimir(String inputFilePath, String outputFilePath) {
        try (
            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFilePath)));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFilePath))
        ) {
            Dicionario dicionario = new Dicionario(TAM_DIC);
            int nextCode = TAM_DIC + 1;
            int currentCode;
            if (inputStream.available() >= 2) {
                currentCode = inputStream.readShort();
            } else {
                currentCode = -1;
            }
            StringBuilder outputSequence = new StringBuilder(dicionario.getSequence(currentCode));

            while (currentCode != -1) {
                if (inputStream.available() >= 2) {
                    currentCode = inputStream.readShort();
                } else {
                    currentCode = -1;
                }

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
}