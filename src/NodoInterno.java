public class NodoInterno {
    private final Paciente dato;
    private NodoInterno siguiente;

    public NodoInterno(Paciente dato) {
        this.dato = dato;
    }

    public Paciente getDato() { return dato; }
    public NodoInterno getSiguiente() { return siguiente; }
    public void setSiguiente(NodoInterno siguiente) { this.siguiente = siguiente; }
}
