import java.util.*;

public class Huffman {
    private final int TAM_BYTE = 256;

    public void comprimir(byte[] data) {
        // Contagem da frequência dos bytes
        int[] frequencias = contarFrequencia(data);

        // Construção da árvore de Huffman
        NodoHuf raiz = construtorArvHuf(frequencias);

        // Construção do mapa de codificação
        Map<Byte, String> codMap = construtorCodMap(raiz);

        // Compressão dos bytes
        StringBuilder dataCod = new StringBuilder();
        for (byte b : data) {
            dataCod.append(codMap.get(b));
        }
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

    private Map<Byte, String> construtorCodMap(NodoHuf raiz) {
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
}
