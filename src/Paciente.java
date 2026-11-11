import java.time.LocalDateTime;

public class Paciente {
    public enum Tipo { REGULAR, EMERGENCIA }

    private final String id;
    private final String nombre;
    private final Tipo tipo;
    private final LocalDateTime registro;

    public Paciente(String id, String nombre, Tipo tipo, LocalDateTime registro) {
        if (id == null || id.isBlank() || nombre == null || nombre.isBlank()
                || tipo == null || registro == null) {
            throw new IllegalArgumentException("ID, nombre, tipo y registro son obligatorios");
        }
        this.id = id.strip();
        this.nombre = nombre.strip();
        this.tipo = tipo;
        this.registro = registro;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public Tipo getTipo() { return tipo; }
    public LocalDateTime getRegistro() { return registro; }
}
