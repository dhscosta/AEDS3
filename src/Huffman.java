import java.util.*;
import java.io.*;

public class Huffman {
    private final int TAM_BYTE = 256;
    private NodoHuf raiz;

    Huffman(){
        raiz = null;
    }

    private int[] contarFrequencia(byte[] data) {
        int[] frequencias = new int[TAM_BYTE];
        for (byte b : data) {
            frequencias[b & 0xFF]++;
        }
        return frequencias;
    }

    private NodoHuf construtorArvHuf(int[] frequencias) {
        PriorityQueue<NodoHuf> pq = new PriorityQueue<>();

        for (int i = 0; i < TAM_BYTE; i++) {
            if (frequencias[i] > 0) {
                pq.offer(new NodoHuf((byte) i, frequencias[i], null, null));
            }
        }

        while (pq.size() > 1) {
            NodoHuf esq = pq.poll();
            NodoHuf dir = pq.poll();
            NodoHuf pai = new NodoHuf((byte) 0, esq.frequencia + dir.frequencia, esq, dir);
            pq.offer(pai);
        }

        return pq.poll();
    }

    private Map<Byte, String> construtorCodMap() {
        Map<Byte, String> codMap = new HashMap<>();
        construtorCodMapRec(raiz, "", codMap);
        return codMap;
    }

    private void construtorCodMapRec(NodoHuf nodo, String prefixo, Map<Byte, String> codMap) {
        if (nodo.esq == null && nodo.dir == null) {
            codMap.put(nodo.chave, prefixo);
        } else {
            construtorCodMapRec(nodo.esq, prefixo + "0", codMap);
            construtorCodMapRec(nodo.dir, prefixo + "1", codMap);
        }
    }

    private byte[] convtoByte(String codData) {
        int length = (codData.length() + 7) / 8;
        byte[] bytes = new byte[length];

        for (int i = 0; i < codData.length(); i += 8) {
            String byteString = codData.substring(i, Math.min(i + 8, codData.length()));
            bytes[i / 8] = (byte) Integer.parseInt(byteString, 2);
        }

        return bytes;
    }

    public byte[] read(String arquivo)throws FileNotFoundException, IOException{
        File arq = new File(arquivo);
        byte[] b = new byte[(int) arq.length()];

        try(FileInputStream fis = new FileInputStream(arq)){
            fis.read(b);
        }

        return b;
    }

    public byte[] comprimir(byte[] data) {
        // Contagem da frequência dos bytes
        int[] frequencias = contarFrequencia(data);

        // Construção da árvore de Huffman
        raiz = construtorArvHuf(frequencias);

        // Construção do mapa de codificação
        Map<Byte, String> codMap = construtorCodMap();

        // Compressão dos bytes
        StringBuilder dataCod = new StringBuilder();
        for (byte b : data) {
            dataCod.append(codMap.get(b));
        }

        return convtoByte(dataCod.toString());
    }

    public byte[] descomprimir(byte[] dataCodificado) {
        StringBuilder dataDecodificado = new StringBuilder();
        NodoHuf nodoAtual = raiz;

        for (byte b : dataCodificado) {
            for (int i = 7; i >= 0; i--) {
                int bit = (b >> i) & 1;

                if (bit == 0) {
                    nodoAtual = nodoAtual.esq;
                } else if (bit == 1) {
                    nodoAtual = nodoAtual.dir;
                }

                if (nodoAtual.esq == null && nodoAtual.dir == null) {
                    dataDecodificado.append(nodoAtual.chave);
                    nodoAtual = raiz;
                }
            }
        }

        return dataDecodificado.toString().getBytes();
    }

    /*public static void main(String[] args) {
        byte[] data = {1, 1, 1, 2, 2, 3, 3, 3, 3, 3};

        Huffman huffman = new Huffman();
        System.out.println(data.length);
        data = huffman.comprimir(data);
        System.out.println(data.length);
        data = huffman.descomprimir(data);
        System.out.println(data.length);
    }*/
}
