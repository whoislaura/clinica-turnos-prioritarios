import java.time.LocalDateTime;

public class Clinica {
    private final Multilista especialidades = new Multilista();
    private final LinkedStack<Cancelacion> cancelaciones = new LinkedStack<>();

    public Multilista getEspecialidades() { return especialidades; }
    public LinkedStack<Cancelacion> getCancelaciones() { return cancelaciones; }

    public void agregarEspecialidad(String nombre) {
        throw new UnsupportedOperationException("Pendiente: validar y agregar especialidad");
    }

    public void registrarPaciente(String especialidad, Paciente paciente) {
        throw new UnsupportedOperationException("Pendiente: registrar en multilista y cola");
    }

    public Paciente atenderSiguiente(String especialidad, LocalDateTime ahora) {
        throw new UnsupportedOperationException("Pendiente: expirar y atender por prioridad");
    }

    public void cancelarTurno(String idPaciente, LocalDateTime ahora) {
        throw new UnsupportedOperationException("Pendiente: retirar y apilar cancelación");
    }

    public void expirarRegulares(LocalDateTime ahora) {
        throw new UnsupportedOperationException("Pendiente: retirar esperas superiores a 30 minutos");
    }

    public int totalAbandonosRecursivo() {
        throw new UnsupportedOperationException("Pendiente: recorrer especialidades recursivamente");
    }
}
