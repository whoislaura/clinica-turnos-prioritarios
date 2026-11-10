import java.time.LocalDateTime;

public class Paciente {
    public enum Tipo { REGULAR, EMERGENCIA }

    private final String id;
    private final String nombre;
    private final Tipo tipo;
    private final LocalDateTime registro;

    public Paciente(String id, String nombre, Tipo tipo, LocalDateTime registro) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.registro = registro;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public Tipo getTipo() { return tipo; }
    public LocalDateTime getRegistro() { return registro; }
}
