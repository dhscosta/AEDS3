//dependencias
import java.util.*;
import java.io.*;

//Classe para compressão de dados
public class Huffman {
    private final int TAM_BYTE = 256;   //tamanho máximo possível dos bytes
    private NodoHuf raiz;               //raiz da árvore

    //Construtor padrão
    Huffman(){
        raiz = null;
    }

    //Método para contar a frequencia dos símbolos 
    private int[] contarFrequencia(byte[] data) {
        int[] frequencias = new int[TAM_BYTE];
        for (byte b : data) {
            frequencias[b & 0xFF]++;
        }
        return frequencias;
    }

    //Método para construir a árvore para auxiliar na compressão
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

    //Método que chama um outro método recursivo, para o mapeamento dos símbolos
    private Map<Byte, String> construtorCodMap() {
        Map<Byte, String> codMap = new HashMap<>();
        construtorCodMapRec(raiz, "", codMap);
        return codMap;
    }

    //Método que de fato mapeia os símbolos
    private void construtorCodMapRec(NodoHuf nodo, String prefixo, Map<Byte, String> codMap) {
        if (nodo.esq == null && nodo.dir == null) {
            codMap.put(nodo.chave, prefixo);
        } else {
            construtorCodMapRec(nodo.esq, prefixo + "0", codMap);
            construtorCodMapRec(nodo.dir, prefixo + "1", codMap);
        }
    }

    //Método que converte a string para um byte
    private byte[] convtoByte(String codData) {
        int length = (codData.length() + 7) / 8;
        byte[] bytes = new byte[length];

        for (int i = 0; i < codData.length(); i += 8) {
            String byteString = codData.substring(i, Math.min(i + 8, codData.length()));
            bytes[i / 8] = (byte) Integer.parseInt(byteString, 2);
        }

        return bytes;
    }

    //Método para transformar um arquivo binário em um array de bytes
    public byte[] read(String arquivo)throws FileNotFoundException, IOException{
        File arq = new File(arquivo);
        byte[] b = new byte[(int) arq.length()];

        try(FileInputStream fis = new FileInputStream(arq)){
            fis.read(b);
        }

        return b;
    }

    //Método que de fato comprime o array de bytes originário do arquivo
    public byte[] comprimir(byte[] data) {
        int[] frequencias = contarFrequencia(data);

        raiz = construtorArvHuf(frequencias);

        Map<Byte, String> codMap = construtorCodMap();

        StringBuilder dataCod = new StringBuilder();
        for (byte b : data) {
            dataCod.append(codMap.get(b));
        }

        return convtoByte(dataCod.toString());
    }

    
    //Método para descompressão do array de bytes
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
}
