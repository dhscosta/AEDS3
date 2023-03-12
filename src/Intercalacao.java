
//dependencias
import java.io.*;
import java.util.ArrayList;

class Nodo {
    public int segmento;
    public Game jogo;

    Nodo() {
        segmento = 0;
        jogo = new Game();
    }

    Nodo(int seg, Game registro) {
        segmento = seg;
        jogo = registro;
    }

    public int filhoEsquerdo(int i) {
        return (i * 2 + 1);
    }

    public int filhoDireito(int i) {
        return (i * 2 + 2);
    }

    public int pai(int i) {
        return (int) ((i - 1) / 2);
    }
}

// Classe para ordenação por intercalação
class Intercalacao {
    // construtor
    Intercalacao() {

    }

    /*ordena - Método para ordenar a partição o vetor. Este método é usado para o quickSort
     *Parâmetros - Game[], int inicio, int fim - arranjo de games, posicao inicial e final
     *Retorno - int
    */
    private int ordena(Game[] vetor, int inicio, int fim) {
        Game pivo = vetor[inicio];
        int i = inicio + 1, f = fim;

        while (i <= f) {
            if (vetor[i].getTitle().compareTo(pivo.getTitle()) == 0
                    || vetor[i].getTitle().compareTo(pivo.getTitle()) == -1)
                i++;
            else if (pivo.getTitle().compareTo(vetor[f].getTitle()) == -1)
                f--;
            else {
                //swap
                Game troca = vetor[i];
                vetor[i] = vetor[f];
                vetor[f] = troca;
                i++;
                f--;
            }
        }
        vetor[inicio] = vetor[f];
        vetor[f] = pivo;
        return f;
    }

     /*quickSort - Método para fazer as partições e fazer a ordenação
     *Parâmetros - Game[], int inicio, int fim - arranjo de games, posicao inicial e final
     *Retorno - void
    */
    private void quickSort(Game[] vetor, int inicio, int fim) {
        if (inicio < fim) {
            int posicaoPivo = ordena(vetor, inicio, fim);
            quickSort(vetor, inicio, posicaoPivo - 1);
            quickSort(vetor, posicaoPivo + 1, fim);
        }
    }

     /*balanceadaComum - Método de intercalação balanceada comum
     *Parâmetros -void
     *Retorno - void
    */
    public void balanceadaComum() throws FileNotFoundException, IOException {
        // abrindo arquivo da base de dados
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");

        // arquivos que serão utilizados para a intercalação
        RandomAccessFile temp1 = new RandomAccessFile("temp1.bin", "rw");
        RandomAccessFile temp2 = new RandomAccessFile("temp2.bin", "rw");
        RandomAccessFile temp3 = new RandomAccessFile("temp3.bin", "rw");
        RandomAccessFile temp4 = new RandomAccessFile("temp4.bin", "rw");

        // tamanho fixos de registros para bloco
        int nRegistros = 4;

        // arranjo de Game para ordenação em memória principal
        Game[] jogos = new Game[nRegistros];

        char lapide; // variavel para leitura de lápide
        int tam; // variavel para leitura de tamanho do registro
        byte[] arranjoByte; // arranjo de bytes para armazenar registro
        int arquivos = 1;

        int tamArq1 = 0;
        int tamArq2 = 0;

        // etapa de distribuição
        while (arq.getFilePointer() < arq.length()) {

            // iteração para armazenar registros no arranjo de games
            for (int i = 0; i < nRegistros; i++) {
                if (arq.getFilePointer() < arq.length()) {
                    lapide = arq.readChar();
                    tam = arq.readInt();

                    if (lapide == '#') {
                        arq.skipBytes(tam);
                    } else {
                        arranjoByte = new byte[tam];
                        arq.read(arranjoByte);
                        jogos[i] = new Game();
                        jogos[i].fromByte(arranjoByte);
                    }
                } else {
                    jogos[i] = new Game();
                }
            }

            // ordenação do arranjo de Games
            quickSort(jogos, 0, jogos.length - 1);

            // testar se esta ordenado
            for (int z = 0; z < jogos.length; z++)
            {
                jogos[z].mostrar();
            }

            // iteração para registrar bloco no arquivo temporário
            for (int i = 0; i < nRegistros; i++) {
                if (arquivos == 1 && jogos[i].getId() != -1) {
                    arranjoByte = jogos[i].toByte();
                    temp1.writeInt(arranjoByte.length);
                    temp1.write(arranjoByte);
                    tamArq1++;
                } else if (jogos[i].getId() != -1) {
                    arranjoByte = jogos[i].toByte();
                    temp2.writeInt(arranjoByte.length);
                    temp2.write(arranjoByte);
                    tamArq2++;
                }
            }

            if (arquivos == 1) {
                arquivos++;
            } else {
                arquivos--;
            }

        }

        long indice1, indice2;
        int maior = (tamArq1 > tamArq2) ? tamArq1 : tamArq2;
        String title1, title2;
        int tam1, tam2, opcao = 0, option = 0;
        RandomAccessFile arq1, arq2, arq3, arq4, destino;
        Game teste = new Game();
        Game teste2 = new Game();

        while (nRegistros < arq.length()) {
            if (option == 0) {
                arq1 = new RandomAccessFile("temp1.bin", "rw");
                arq2 = new RandomAccessFile("temp2.bin", "rw");
                arq3 = new RandomAccessFile("temp3.bin", "rw");
                arq4 = new RandomAccessFile("temp4.bin", "rw");

                option = 1;
            } else {
                arq1 = new RandomAccessFile("temp3.bin", "rw");
                arq2 = new RandomAccessFile("temp4.bin", "rw");
                arq3 = new RandomAccessFile("temp1.bin", "rw");
                arq4 = new RandomAccessFile("temp2.bin", "rw");

                option = 0;
            }

            for (int i = 0; i < maior; i++) {
                if (opcao == 0) {
                    destino = arq3;
                    opcao = 1;

                } else {
                    destino = arq4;
                    opcao = 0;
                }

                for (int j = 0; j < nRegistros; j++) {
                    indice1 = arq1.getFilePointer();
                    indice2 = arq2.getFilePointer();

                    tam1 = arq1.readInt();
                    tam2 = arq2.readInt();

                    byte[] tester = new byte[tam1];
                    arq1.read(tester);
                    teste.fromByte(tester);
                    title1 = teste.getTitle();

                    byte[] tester2 = new byte[tam2];
                    arq2.read(tester2);
                    teste2.fromByte(tester2);
                    title2 = teste2.getTitle();

                    if (title1.compareTo(title2) == -1) {
                        arq1.seek(indice1);
                        arranjoByte = new byte[tam1];
                        arq1.read(arranjoByte);

                        destino.writeInt(tam1);
                        destino.write(arranjoByte);
                    } else {
                        arq2.seek(indice2);
                        arranjoByte = new byte[tam2];
                        arq2.read(arranjoByte);

                        destino.writeInt(tam1);
                        destino.write(arranjoByte);
                    }
                }
            }

            nRegistros *= 2;
        }

        // fechamento de arquivos
        arq.close();
        temp1.close();
        temp2.close();
        temp3.close();
        temp4.close();

    }

    /*balanceadaVariavel - Método de intercalação balanceada variavel
     *Parâmetros -void
     *Retorno - void
    */
    public void balanceadaVariavel() throws FileNotFoundException, IOException {
        // abrindo arquivo da base de dados
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");

        // arquivos que serão utilizados para a intercalação
        RandomAccessFile temp1 = new RandomAccessFile("temp1.bin", "rw");
        RandomAccessFile temp2 = new RandomAccessFile("temp2.bin", "rw");
        RandomAccessFile temp3 = new RandomAccessFile("temp3.bin", "rw");
        RandomAccessFile temp4 = new RandomAccessFile("temp4.bin", "rw");

        // tamanho fixos de registros para bloco
        int nRegistros = 4;

        // arranjo de Game para ordenação em memória principal
        Game[] jogos = new Game[nRegistros];

        char lapide; // variavel para leitura de lápide
        int tam; // variavel para leitura de tamanho do registro
        byte[] arranjoByte; // arranjo de bytes para armazenar registro
        int arquivos = 1;

        // etapa de distribuição
        while (arq.getFilePointer() < arq.length()) {

            // iteração para armazenar registros no arranjo de games
            for (int i = 0; i < nRegistros; i++) {
                if (arq.getFilePointer() < arq.length()) {
                    lapide = arq.readChar();
                    tam = arq.readInt();

                    if (lapide == '#') {
                        arq.skipBytes(tam);
                    } else {
                        arranjoByte = new byte[tam];
                        arq.read(arranjoByte);
                        jogos[i] = new Game();
                        jogos[i].fromByte(arranjoByte);
                    }
                } else {
                    jogos[i] = new Game();
                }
            }

            // ordenação do arranjo de Games
            quickSort(jogos, 0, jogos.length - 1);

            // iteração para registrar bloco no arquivo temporário
            for (int i = 0; i < nRegistros; i++) {
                if (arquivos == 1 && jogos[i].getId() != -1) {
                    arranjoByte = jogos[i].toByte();
                    temp1.writeInt(arranjoByte.length);
                    temp1.write(arranjoByte);
                } else if (jogos[i].getId() != -1) {
                    arranjoByte = jogos[i].toByte();
                    temp2.writeInt(arranjoByte.length);
                    temp2.write(arranjoByte);
                }
            }

            if (arquivos == 1) {
                arquivos++;
            } else {
                arquivos--;
            }

        }

        long indice1, indice2;
        long maior = (temp1.length() > temp2.length()) ? temp1.length() : temp2.length();
        int tam1, tam2, opcao = 0, option = 0, maior2, menor1 = 0, menor2 = 0;
        String title1, title2;
        byte[] arranjo1, arranjo2;
        Game gamer = new Game();
        RandomAccessFile arq1, arq2, arq3, arq4, destino;

        while (nRegistros < arq.length()) {
            if (option == 0) {
                arq1 = new RandomAccessFile("temp1.bin", "rw");
                arq2 = new RandomAccessFile("temp2.bin", "rw");
                arq3 = new RandomAccessFile("temp3.bin", "rw");
                arq4 = new RandomAccessFile("temp4.bin", "rw");

                option = 1;
            } else {
                arq1 = new RandomAccessFile("temp3.bin", "rw");
                arq2 = new RandomAccessFile("temp4.bin", "rw");
                arq3 = new RandomAccessFile("temp1.bin", "rw");
                arq4 = new RandomAccessFile("temp2.bin", "rw");

                option = 0;
            }

            for (int i = 0; i < maior; i++) {
                if (opcao == 0) {
                    destino = arq3;
                    opcao = 1;

                } else {
                    destino = arq4;
                    opcao = 0;
                }

                ArrayList<Game> bloco1 = new ArrayList<Game>(nRegistros);
                ArrayList<Game> bloco2 = new ArrayList<Game>(nRegistros);

                if (i < nRegistros) {
                    tam1 = arq1.readInt();
                    tam2 = arq2.readInt();

                    arranjo1 = new byte[tam1];
                    arranjo2 = new byte[tam2];

                    arq1.read(arranjo1);
                    arq2.read(arranjo2);

                    gamer.fromByte(arranjo1);
                    bloco1.add(gamer);
                    gamer.fromByte(arranjo2);
                    bloco2.add(gamer);
                } else {
                    indice1 = arq1.getFilePointer();
                    indice2 = arq2.getFilePointer();

                    tam1 = arq1.readInt();
                    tam2 = arq2.readInt();

                    arranjo1 = new byte[tam1];
                    arranjo2 = new byte[tam2];

                    arq1.read(arranjo1);
                    arq2.read(arranjo2);

                    gamer.fromByte(arranjo1);
                    if (gamer.getTitle().compareTo(bloco1.get(bloco1.size()).getTitle()) == 1) {
                        bloco1.add(gamer);
                    } else {
                        arq1.seek(indice1);
                    }

                    gamer.fromByte(arranjo2);
                    if (gamer.getTitle().compareTo(bloco2.get(bloco2.size()).getTitle()) == 1) {
                        bloco2.add(gamer);
                    } else {
                        arq2.seek(indice2);
                    }
                }

                maior2 = (bloco1.size() < bloco2.size()) ? bloco2.size() : bloco1.size();

                for (int j = 0; j < maior2; j++) {
                    title1 = bloco1.get(j).getTitle();
                    title2 = bloco2.get(j).getTitle();

                    if (j == 0) {
                        menor1 = menor2 = -2;
                    }

                    if (title1.compareTo(title2) == -1) {
                        arranjo1 = (menor1 == j - 1) ? bloco1.get(menor1).toByte() : bloco1.get(j).toByte();
                        destino.writeInt(arranjo1.length);
                        destino.write(arranjo1);
                        menor2 = j;
                    } else {
                        arranjo2 = (menor2 == j - 1) ? bloco2.get(menor2).toByte() : bloco2.get(j).toByte();
                        destino.writeInt(arranjo2.length);
                        destino.write(arranjo2);
                        menor1 = j;
                    }
                }
            }

            nRegistros *= 2;
        }

        // fechamento de arquivos
        arq.close();
        temp1.close();
        temp2.close();
        temp3.close();
        temp4.close();

    }

    /*heap - Método de intercalação por substituição
     *Parâmetros -void
     *Retorno - void
    */
    public void heap() throws FileNotFoundException, IOException {
        // abrindo arquivo da base de dados
        RandomAccessFile arq = new RandomAccessFile("gamees.bin", "rw");

        // arquivos que serão utilizados para a intercalação
        RandomAccessFile temp1 = new RandomAccessFile("temp1.bin", "rw");
        RandomAccessFile temp2 = new RandomAccessFile("temp2.bin", "rw");
        RandomAccessFile destino;

        ArrayList<Nodo> lista = new ArrayList<Nodo>(6);
        //inicialização de variaveis
        byte[] bytes;
        int tam = 0, segmento = 0, antseg = 0;
        Game jogo = new Game(), ultJogo = new Game();
        Nodo no = new Nodo(), pos0 = new Nodo(), pos1 = new Nodo(), pos2 = new Nodo(),
           pos3 = new Nodo(), pos4 = new Nodo(), pos5 = new Nodo(), pos6 = new Nodo();

        for (int i = 0; i < arq.length(); i++) {
            if (antseg == segmento) {
                destino = new RandomAccessFile("temp1.bin", "rw");
            } else {
                destino = new RandomAccessFile("temp2.bin", "rw");
            }

            if (i < 7) {
                tam = arq.readInt();
                bytes = new byte[tam];
                arq.read(bytes);
                jogo.fromByte(bytes);
                no = new Nodo(segmento, jogo);
                lista.add(no);
            } else {
                pos0 = lista.get(0);
                pos1 = lista.get(1);
                pos2 = lista.get(2);
                pos3 = lista.get(3);
                pos4 = lista.get(4);
                pos5 = lista.get(5);
                pos6 = lista.get(6);

                bytes = pos0.jogo.toByte();
                ultJogo = pos0.jogo;
                destino.writeInt(pos0.segmento);
                destino.writeInt(bytes.length);
                destino.write(bytes);

                tam = arq.readInt();
                bytes = new byte[tam];
                arq.read(bytes);
                jogo.fromByte(bytes);
                pos0 = new Nodo(segmento, jogo);

                lista.add(0, no);

                if (pos0.jogo.getTitle().compareTo(pos1.jogo.getTitle()) == 1) {
                    Nodo noTemp = new Nodo();
                    noTemp = pos1;
                    pos1.segmento = pos0.segmento;
                    pos1.jogo = pos0.jogo;

                    pos0 = noTemp;

                    if (pos1.jogo.getTitle().compareTo(pos3.jogo.getTitle()) == 1) {
                        noTemp = pos1;
                        pos1.segmento = pos3.segmento;
                        pos1.jogo = pos3.jogo;

                        pos3 = noTemp;
                    } else if (pos1.jogo.getTitle().compareTo(pos4.jogo.getTitle()) == 1) {
                        noTemp = pos1;
                        pos1.segmento = pos4.segmento;
                        pos1.jogo = pos4.jogo;

                        pos4 = noTemp;
                    }

                } else if (pos0.jogo.getTitle().compareTo(pos2.jogo.getTitle()) == 1) {
                    Nodo noTemp = new Nodo();
                    noTemp = pos2;
                    pos2.segmento = pos0.segmento;
                    pos2.jogo = pos0.jogo;

                    pos0 = noTemp;

                    if (pos2.jogo.getTitle().compareTo(pos5.jogo.getTitle()) == 1) {
                        noTemp = pos2;
                        pos2.segmento = pos5.segmento;
                        pos2.jogo = pos5.jogo;

                        pos5 = noTemp;
                    } else if (pos2.jogo.getTitle().compareTo(pos6.jogo.getTitle()) == 1) {
                        noTemp = pos2;
                        pos2.segmento = pos6.segmento;
                        pos2.jogo = pos6.jogo;

                        pos6 = noTemp;
                    }
                }

                if(pos0.jogo.getTitle().compareTo(ultJogo.getTitle()) == - 1){
                    antseg = segmento;
                    segmento = segmento + 1;
                    pos0.segmento = segmento;

                    if (pos0.segmento > pos1.segmento) {
                        Nodo noTemp = new Nodo();
                        noTemp = pos1;
                        pos1.segmento = pos0.segmento;
                        pos1.jogo = pos0.jogo;
    
                        pos0 = noTemp;
    
                        if (pos1.segmento > pos3.segmento) {
                            noTemp = pos1;
                            pos1.segmento = pos3.segmento;
                            pos1.jogo = pos3.jogo;
    
                            pos3 = noTemp;
                        } else if (pos1.segmento > pos4.segmento) {
                            noTemp = pos1;
                            pos1.segmento = pos4.segmento;
                            pos1.jogo = pos4.jogo;
    
                            pos4 = noTemp;
                        }
    
                    } else if (pos0.segmento > pos2.segmento) {
                        Nodo noTemp = new Nodo();
                        noTemp = pos2;
                        pos2.segmento = pos0.segmento;
                        pos2.jogo = pos0.jogo;
    
                        pos0 = noTemp;
    
                        if(pos2.segmento > pos5.segmento){
                            noTemp = pos2;
                            pos2.segmento = pos5.segmento;
                            pos2.jogo = pos5.jogo;
    
                            pos5 = noTemp;
                        }else if(pos2.segmento > pos6.segmento){
                            noTemp = pos2;
                            pos2.segmento = pos6.segmento;
                            pos2.jogo = pos6.jogo;
    
                            pos6 = noTemp;
                        }
                    }
                }else{
                    bytes = pos0.jogo.toByte();
                    ultJogo = pos0.jogo;
                    destino.writeInt(pos0.segmento);
                    destino.writeInt(bytes.length);
                    destino.write(bytes);
                }
            }
        }

        long maior = (temp1.length() > temp2.length()) ? temp1.length(): temp2.length();
        long i1, i2;
        Nodo no1 = new Nodo(), no2 = new Nodo();
        int tam1, tam2, seg1, seg2;
        Game teste = new Game(), teste2 = new Game();
        String title1, title2;

        for(int i = 0; i < maior; i++){
            i1 = temp1.getFilePointer();
            i2 = temp2.getFilePointer();

            seg1 = temp1.readInt();
            seg2 = temp2.readInt();

            tam1 = temp1.readInt();
            tam2 = temp2.readInt();

            byte[] tester = new byte[tam1];
            temp1.read(tester);
            teste.fromByte(tester);
            title1 = teste.getTitle();

            byte[] tester2 = new byte[tam2];
            temp2.read(tester2);
            teste2.fromByte(tester2);
            title2 = teste2.getTitle();
        }

        arq.close();
        temp1.close();
        temp2.close();
    }
}