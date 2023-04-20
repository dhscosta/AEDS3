import java.io.*;
import java.util.Scanner;

public class ArqIndices {

    Diretorio diretorio;
    String arqDir;
    String arqBuc;
    RandomAccessFile fileDir;
    RandomAccessFile fileBuc;
    int bucTamanho;

    class Bucket {
        private int[] ids;
        private long[] registros;
        private int quantidade;
        private int profundidadeLocal;
    
        public Bucket(int tamanho, int profundidade) throws Exception {
            if(tamanho > 1060){
                throw new Exception("A quantidade maxima de cada bucket e de 1060 elementos");
            }
            if(profundidade > 10){
                throw new Exception("A profundidade máxima é de 10 ");
            }
            this.ids = new int[tamanho];
            this.registros = new long[tamanho];
            this.quantidade = 0;
            this.profundidadeLocal = profundidade;
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            dos.writeShort(quantidade);
            dos.writeShort(profundidadeLocal);
            int x=0;
            while(x < quantidade) {
                dos.writeInt(ids[x]);
                dos.writeLong(registros[x]);
                x++;
            }
            while(x < 1060) {
                dos.writeInt(0);
                dos.writeLong(0);
                x++;
            }
            return baos.toByteArray();            
        }

        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            quantidade = dis.readShort();
            profundidadeLocal = dis.readByte();

            int i=0;
            while(i<1060) {
                ids[i] = dis.readInt();
                registros[i] = dis.readLong();
                i++;
            }
        }

        public boolean inserir(int y, long reg) throws Exception{
            if(quantidade == 1060)
            {
                return false;
            }

            int x = quantidade - 1;

            while(x >= 0 && y < ids[x]) {
                ids[x+1] = ids[x];
                registros[x+1] = registros[x];
                x--;
            }
            x++;
            ids[x] = y;
            registros[x] = reg;
            quantidade++;
            return true;
        }
    
        public long buscar(int id) {
            if(quantidade == 0){
                return 0;
            }

            int x = 0;
            while (x < quantidade){
                if(id == ids[x]){
                    return registros[x];
                }
                x++;
            }
            return 0;
        }
        

    }
    
    class Diretorio {
        private long[] ends;
        private int profundidadeGlobal;
    
        public Diretorio() {
            this.ends = new long[1];
            ends[0] = 0;
            this.profundidadeGlobal = 0;
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
        
            dos.writeByte(profundidadeGlobal);
            int quantidade = (int)Math.pow(2,profundidadeGlobal);
            int x = 0;
            while(x < quantidade) {
                dos.writeLong(ends[x]);
                x++;
            }
            return baos.toByteArray();            
        }

        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);

            profundidadeGlobal = dis.readByte();
            int quantidade = (int)Math.pow(2,profundidadeGlobal);
            ends = new long[quantidade];
            int x = 0;

            while(x < quantidade) {
                ends[x] = dis.readLong();
                x++;
            }
        }

        private void addProfundidade(){
            this.profundidadeGlobal = profundidadeGlobal++;
        }

        private void setEnds(long[] newEnds){
            this.ends = newEnds;
        }

        private int hash(int id) {
            return (id % (profundidadeGlobal*2));
        }

        private int hashLocal(int id, int local){
            return (id % (local * 2));
        }

        private long getEnd(int p) {
            if( p > Math.pow(2,profundidadeGlobal)){
                return -1;
            }
            return ends[p];
        }

        private boolean newDirEnd(int x, long end) {

            if(x > Math.pow(2,profundidadeGlobal)){
                return false;
            }
            ends[x] = end;
            return true;
        }
    }

    public ArqIndices(int tamanhoBuc) throws Exception{
        fileDir = new RandomAccessFile("diretorio","rw");
        fileBuc = new RandomAccessFile("bucket","rw");

        if(fileDir.length() == 0 || fileBuc.length() == 0){
            this.diretorio = new Diretorio();
            fileDir.write(diretorio.toByteArray());

            Bucket buc = new Bucket(tamanhoBuc, 0);
            fileBuc.seek(0);
            fileBuc.write(buc.toByteArray());
        }
    }

    public boolean inserir(long registro, int id) throws Exception{
        int tam = (int)fileDir.length();
        byte[] b = new byte[tam];
        fileDir.seek(0);
        fileDir.read(b);
        diretorio = new Diretorio();
        diretorio.fromByteArray(b);
        int hash = diretorio.hash(id);

        long endBuc = diretorio.getEnd(hash);
        Bucket buc = new Bucket(1060, 0);
        byte[] bucByte = new byte[12723];
        fileBuc.seek(endBuc);
        fileBuc.read(bucByte);
        buc.fromByteArray(bucByte);

        if(buc.buscar(id) != 0){
            throw new Exception("Id repetido");
        }

        if(buc.quantidade < 1060){
            buc.inserir(id, registro);
            fileBuc.seek(endBuc);
            fileBuc.write(buc.toByteArray());
            return true;
        }
        else{
            int profLoc = buc.profundidadeLocal;
            int profGlob = diretorio.profundidadeGlobal;
            if(buc.profundidadeLocal >= diretorio.profundidadeGlobal || diretorio.profundidadeGlobal < 10){
                    diretorio.addProfundidade();
                    long[] newEnds = new long [(int)Math.pow(2,diretorio.profundidadeGlobal)];
                    int x = 0;
                    while ( x < (int)Math.pow(2, diretorio.profundidadeGlobal-1)){
                        newEnds[x] = diretorio.ends[x];
                        x++;
                    }
                    while (x < (int)Math.pow(2, diretorio.profundidadeGlobal)){
                        newEnds[x] = diretorio.ends[x - (int)Math.pow(2, diretorio.profundidadeGlobal-1) ];
                        x++;
                    }
                    diretorio.setEnds(newEnds);
            }
            else{
                throw new Exception("Não é possivel inserir mais, pois está cheio");
            }

            Bucket buc2 = new Bucket(1060, buc.profundidadeLocal+1);
            fileBuc.seek(endBuc);
            fileBuc.write(buc2.toByteArray());

            Bucket buc3 = new Bucket(1060, buc.profundidadeLocal + 1);
            long newEndBuc = fileBuc.length();
            fileBuc.seek(newEndBuc);
            fileBuc.write(buc3.toByteArray());

            int i = diretorio.hashLocal(id, buc.profundidadeLocal);
            int controle = 1;
            for(int x = i; x < (int)Math.pow(2, profLoc) ; x += (int)Math.pow(2, profGlob)){
                if(controle % 2 == 0){
                    diretorio.newDirEnd(x, newEndBuc);
                }
                controle++;
            }

            b = diretorio.toByteArray();
            fileDir.seek(0);
            fileDir.write(b);

            for(int x = 0; x < buc.quantidade; x++){
                inserir(buc.registros[x], buc.ids[x]);
            }
            inserir(registro, id);
            return false;

        }
    }

    public long buscar(int id) throws Exception{
        byte[] b = new byte[(int)fileDir.length()];
        fileDir.seek(0);
        fileDir.read(b);

        this.diretorio = new Diretorio();
        diretorio.fromByteArray(b);

        int hash = diretorio.hash(id);

        long endBuc = diretorio.getEnd(hash);
        Bucket buc = new Bucket(1060, 0);
        byte[] bucByte = new byte[12723];
        fileBuc.seek(endBuc);
        fileBuc.read(bucByte);
        buc.fromByteArray(bucByte);

        return buc.buscar(id);
    }

    public void print() {
        try {
            byte[] bd = new byte[(int)fileDir.length()];
            fileDir.seek(0);
            fileDir.read(bd);
            diretorio = new Diretorio();
            diretorio.fromByteArray(bd);   
            System.out.println("\nDIRETÓRIO ------------------");
            System.out.println(diretorio);

            System.out.println("\nCESTOS ---------------------");
            fileBuc.seek(0);
            while(fileBuc.getFilePointer() != fileBuc.length()) {
                Bucket c = new Bucket(1060, 0);
                byte[] ba = new byte[12723];
                fileBuc.read(ba);
                c.fromByteArray(ba);
                System.out.println(c);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
                
                ArqIndices he;
                Scanner console = new Scanner(System.in);
            
                try {
                    he = new ArqIndices(1060);
        
        
                    int opcao;
                    do {
                        System.out.println("\n\n-------------------------------");
                        System.out.println("              MENU");
                        System.out.println("-------------------------------");
                        System.out.println("1 - Inserir");
                        System.out.println("2 - Buscar");
                        System.out.println("3 - Excluir");
                        System.out.println("4 - Imprimir");
                        System.out.println("0 - Sair");
                        try {
                            opcao = Integer.valueOf(console.nextLine());
                        } catch(NumberFormatException e) {
                            opcao = -1;
                        }
                        
                        switch(opcao) {
                            case 1: {
                                System.out.println("\nINCLUSÃO");
                                System.out.print("Chave: ");
                                int chave = Integer.valueOf(console.nextLine());
                                System.out.print("Dado: ");
                                long dado = Long.valueOf(console.nextLine());
                                he.inserir(dado, chave);
                                he.print();
                            }break;
                            case 2: {
                                System.out.println("\nBUSCA");
                                System.out.print("Chave: ");
                                int chave = Integer.valueOf(console.nextLine());
                                System.out.print("Dado: "+he.buscar(chave));
                            }break;
                            /*
                            case 3: {
                                System.out.println("\nEXCLUSÃO");
                                System.out.print("Chave: ");
                                int chave = Integer.valueOf(console.nextLine());
                                he.delete(chave);
                                he.print();
                            } break;
                             */
                            case 4: {
                                he.print();
                            } break;
                            case 0: break;
                            default: System.out.println("Opção inválida");
                        }
                    } while(opcao != 0);
        
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
}
    /* 
    public int buscar(int id) {
        ArqIndices indice = diretorio.buscar(id);
        return indice.getPosicao();
    }

    public static void main(String[] args) throws Exception {
    
    }
    */
