//Classe dos Nodos, implementando a interface Comparable
class NodoHuf implements Comparable<NodoHuf> {
    byte chave;
    int frequencia;
    NodoHuf esq;
    NodoHuf dir;

    //Construtor da classe
    public NodoHuf(byte chave, int frequencia, NodoHuf esq, NodoHuf dir) {
        this.chave = chave;
        this.frequencia = frequencia;
        this.esq = esq;
        this.dir = dir;
    }

    //Método de comparação entre nodos
    @Override
    public int compareTo(NodoHuf node) {
        return this.frequencia - node.frequencia;
    }
}
