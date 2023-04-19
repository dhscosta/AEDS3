import java.io.*;

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

        
        private int hash(int id) {
            return (id % (profundidadeGlobal*2));
        }

        private long end(int p) {
            if( p > Math.pow(2,profundidadeGlobal)){
                return -1;
            }
            return ends[p];
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

        long endBuc = diretorio.end(hash);
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
}
