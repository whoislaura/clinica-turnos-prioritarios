import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        Clinica clinica = new Clinica();
        clinica.agregarEspecialidad("General");
        clinica.agregarEspecialidad("Pediatría");
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 10, 8, 0);
        clinica.registrarPaciente("General", new Paciente("R1", "Ana", Paciente.Tipo.REGULAR, inicio));
        clinica.registrarPaciente("General", new Paciente("R2", "Luis", Paciente.Tipo.REGULAR,
                inicio.plusMinutes(1)));
        clinica.registrarPaciente("General", new Paciente("E1", "Eva", Paciente.Tipo.EMERGENCIA,
                inicio.plusMinutes(2)));
        clinica.cancelarTurno("R2", inicio.plusMinutes(3));
        System.out.println("Última cancelación manual: "
                + clinica.getCancelaciones().top().getPaciente().getNombre());
        System.out.println("Se atiende primero a: "
                + clinica.atenderSiguiente("General", inicio.plusMinutes(4)).getNombre());
        clinica.registrarPaciente("Pediatría", new Paciente("R3", "Leo", Paciente.Tipo.REGULAR,
                inicio.plusMinutes(5)));
        clinica.expirarRegulares(inicio.plusMinutes(36));
        NodoExterno actual = clinica.getEspecialidades().getCabeza();
        while (actual != null) {
            NodoInterno abandono = actual.getDato().getHistorialAbandonos();
            while (abandono != null) {
                System.out.println(abandono.getDato().getNombre() + " (" + actual.getDato().getNombre()
                        + "): abandonado por tiempo excedido");
                abandono = abandono.getSiguiente();
            }
            actual = actual.getSiguiente();
        }
        System.out.println("Total recursivo de abandonos: " + clinica.totalAbandonosRecursivo());
    }
}
