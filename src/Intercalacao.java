
//dependencias
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.DataFormatException;

//Classe para ordenação por intercalação
class Intercalacao {
    // construtor
    Intercalacao() {

    }

    // Método para ordenar a partição o vetor. Este método é usado para o quickSort
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

    // função para fazer as partições e executar a ordenação
    private void quickSort(Game[] vetor, int inicio, int fim) {
        if (inicio < fim) {
            int posicaoPivo = ordena(vetor, inicio, fim);
            quickSort(vetor, inicio, posicaoPivo - 1);
            quickSort(vetor, posicaoPivo + 1, fim);
        }
    }

    // Método de intercalação balanceada comum
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

                    title1 = arq1.readUTF();
                    title2 = arq2.readUTF();

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
}

/*
 * int j = 0;
 * while(j < tamArq1 || j < tamArq2){
 * if(j < tamArq1){
 * tam = temp1.readInt();
 * arranjoByte = new byte[tam];
 * temp1.read(arranjoByte);
 * tempGame1[j].fromByte(arranjoByte);
 * }
 * 
 * if(j < tamArq2){
 * tam = temp2.readInt();
 * arranjoByte = new byte[tam];
 * temp2.read(arranjoByte);
 * tempGame2[j].fromByte(arranjoByte);
 * }
 * 
 * j++;
 * }
 * 
 * for(int i = 0; i < nRegistros; i++){
 * if(tempGame1[i].getTitle().compareTo(tempGame2[i].getTitle()) == -1){
 * arranjoByte = tempGame1[i].toByte();
 * temp3.writeInt(arranjoByte.length);
 * temp3.write(arranjoByte);
 * }else{
 * arranjoByte = tempGame2[i].toByte();
 * temp3.writeInt(arranjoByte.length);
 * temp3.write(arranjoByte);
 * }
 * 
 * if(i == (nRegistros-1) && ){
 * i = 0;
 * }
 * }
 */