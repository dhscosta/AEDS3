class NodoHuf implements Comparable<NodoHuf> {
    byte chave;
    int frequencia;
    NodoHuf esq;
    NodoHuf dir;

    public NodoHuf(byte chave, int frequencia, NodoHuf esq, NodoHuf dir) {
        this.chave = chave;
        this.frequencia = frequencia;
        this.esq = esq;
        this.dir = dir;
    }

    @Override
    public int compareTo(NodoHuf node) {
        return this.frequencia - node.frequencia;
    }
}
