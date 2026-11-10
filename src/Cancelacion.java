import java.time.LocalDateTime;

public class Cancelacion {
    private final Paciente paciente;
    private final String especialidad;
    private final LocalDateTime momento;

    public Cancelacion(Paciente paciente, String especialidad, LocalDateTime momento) {
        this.paciente = paciente;
        this.especialidad = especialidad;
        this.momento = momento;
    }

    public Paciente getPaciente() { return paciente; }
    public String getEspecialidad() { return especialidad; }
    public LocalDateTime getMomento() { return momento; }
}
