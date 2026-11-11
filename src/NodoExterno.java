public class NodoExterno {
    private final Especialidad dato;
    private NodoInterno listaInterna;
    private NodoExterno siguiente;

    public NodoExterno(Especialidad dato) {
        if (dato == null) {
            throw new IllegalArgumentException("La especialidad del nodo es obligatoria");
        }
        this.dato = dato;
    }

    public Especialidad getDato() { return dato; }
    public NodoInterno getListaInterna() { return listaInterna; }
    public void setListaInterna(NodoInterno listaInterna) { this.listaInterna = listaInterna; }
    public NodoExterno getSiguiente() { return siguiente; }
    public void setSiguiente(NodoExterno siguiente) { this.siguiente = siguiente; }
}
