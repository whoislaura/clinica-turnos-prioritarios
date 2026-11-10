public class Especialidad {
    private final String nombre;
    private final LinkedQueue<Paciente> emergencias = new LinkedQueue<>();
    private final LinkedQueue<Paciente> regulares = new LinkedQueue<>();
    private int abandonosPorTiempo;

    public Especialidad(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
    public LinkedQueue<Paciente> getEmergencias() { return emergencias; }
    public LinkedQueue<Paciente> getRegulares() { return regulares; }
    public int getAbandonosPorTiempo() { return abandonosPorTiempo; }

    public void registrarAbandonoPorTiempo() {
        abandonosPorTiempo++;
    }
}
