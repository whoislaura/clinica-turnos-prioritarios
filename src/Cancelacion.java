import java.time.LocalDateTime;

public class Cancelacion {
    private final Paciente paciente;
    private final String especialidad;
    private final LocalDateTime momento;

    public Cancelacion(Paciente paciente, String especialidad, LocalDateTime momento) {
        if (paciente == null || especialidad == null || especialidad.isBlank() || momento == null) {
            throw new IllegalArgumentException("Paciente, especialidad y fecha de cancelación son obligatorios");
        }
        if (momento.isBefore(paciente.getRegistro())) {
            throw new IllegalArgumentException("La cancelación no puede ser anterior al registro");
        }
        this.paciente = paciente;
        this.especialidad = especialidad;
        this.momento = momento;
    }

    public Paciente getPaciente() { return paciente; }
    public String getEspecialidad() { return especialidad; }
    public LocalDateTime getMomento() { return momento; }
}
