public class Especialidad {
    private final String nombre;
    private final LinkedQueue<Paciente> emergencias = new LinkedQueue<>();
    private final LinkedQueue<Paciente> regulares = new LinkedQueue<>();
    private int abandonosPorTiempo;
    private NodoInterno historialAbandonos;

    public Especialidad(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la especialidad es obligatorio");
        }
        this.nombre = nombre.strip();
    }

    public String getNombre() { return nombre; }
    public LinkedQueue<Paciente> getEmergencias() { return emergencias; }
    public LinkedQueue<Paciente> getRegulares() { return regulares; }
    public int getAbandonosPorTiempo() { return abandonosPorTiempo; }
    public NodoInterno getHistorialAbandonos() { return historialAbandonos; }

    public void registrarAbandonoPorTiempo(Paciente paciente) {
        if (paciente == null || paciente.getTipo() != Paciente.Tipo.REGULAR) {
            throw new IllegalArgumentException("El abandono por tiempo requiere un paciente regular");
        }
        NodoInterno nuevo = new NodoInterno(paciente);
        nuevo.setSiguiente(historialAbandonos);
        historialAbandonos = nuevo;
        abandonosPorTiempo++;
    }
}
