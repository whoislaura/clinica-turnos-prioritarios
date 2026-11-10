import java.time.LocalDateTime;

public class PruebaClinica {
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 10, 8, 0);

    public static void main(String[] args) {
        probarPrioridadYFifo();
        probarCancelaciones();
        probarExpiracion();
        probarExpiracionFueraDelFrente();
        probarCancelacionExpirada();
        probarValidaciones();
        System.out.println("Pruebas de clínica: OK");
    }

    private static void probarPrioridadYFifo() {
        Clinica clinica = crearClinica();
        Paciente regular1 = paciente("R1", Paciente.Tipo.REGULAR, INICIO);
        Paciente emergencia1 = paciente("E1", Paciente.Tipo.EMERGENCIA, INICIO.plusMinutes(1));
        Paciente regular2 = paciente("R2", Paciente.Tipo.REGULAR, INICIO.plusMinutes(2));
        Paciente emergencia2 = paciente("E2", Paciente.Tipo.EMERGENCIA, INICIO.plusMinutes(3));
        clinica.registrarPaciente("General", regular1);
        clinica.registrarPaciente("General", emergencia1);
        clinica.registrarPaciente("General", regular2);
        clinica.registrarPaciente("General", emergencia2);
        comprobar(cantidadEnEspera(clinica, "General") == 4, "Registro en multilista");
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(4)) == emergencia1,
                "Emergencia antes que regular");
        comprobar(cantidadEnEspera(clinica, "General") == 3, "Atención retira de la multilista");
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(5)) == emergencia2,
                "FIFO de emergencias");
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(30)) == regular1,
                "A los 30 minutos exactos no expira");
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(30)) == regular2,
                "FIFO de regulares");
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(30)) == null,
                "Atención con colas vacías");
        comprobar(cantidadEnEspera(clinica, "General") == 0, "Sublista vacía tras atender");
        comprobar(clinica.totalAbandonosRecursivo() == 0, "Atender no cuenta como abandono");
        clinica.registrarPaciente("General", regular1);
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(30)) == regular1,
                "Reutilizar cola vacía y registrar un nuevo turno del mismo paciente");
    }

    private static void probarCancelaciones() {
        Clinica clinica = crearClinica();
        Paciente primero = paciente("R1", Paciente.Tipo.REGULAR, INICIO);
        Paciente medio = paciente("R2", Paciente.Tipo.REGULAR, INICIO);
        Paciente ultimo = paciente("R3", Paciente.Tipo.REGULAR, INICIO);
        Paciente emergencia1 = paciente("E1", Paciente.Tipo.EMERGENCIA, INICIO);
        Paciente emergencia2 = paciente("E2", Paciente.Tipo.EMERGENCIA, INICIO);
        clinica.registrarPaciente("General", primero);
        clinica.registrarPaciente("General", medio);
        clinica.registrarPaciente("General", ultimo);
        clinica.registrarPaciente("General", emergencia1);
        clinica.registrarPaciente("General", emergencia2);
        clinica.cancelarTurno("R2", INICIO.plusMinutes(1));
        clinica.cancelarTurno("E1", INICIO.plusMinutes(2));
        clinica.cancelarTurno("R3", INICIO.plusMinutes(3));
        comprobar(cantidadEnEspera(clinica, "General") == 2, "Eliminar cabeza, medio y final");
        Cancelacion reciente = clinica.getCancelaciones().top();
        comprobar(reciente.getPaciente() == ultimo, "Última cancelación en la cima");
        comprobar(reciente.getEspecialidad().equals("General"), "Especialidad de cancelación");
        comprobar(reciente.getMomento().equals(INICIO.plusMinutes(3)), "Hora de cancelación");
        comprobar(clinica.getCancelaciones().pop().getPaciente() == ultimo, "LIFO: último");
        comprobar(clinica.getCancelaciones().pop().getPaciente() == emergencia1, "LIFO: intermedio");
        comprobar(clinica.getCancelaciones().pop().getPaciente() == medio, "LIFO: primero");
        comprobar(clinica.getCancelaciones().isEmpty(), "Pila vacía tras extraer historial");
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(4)) == emergencia2,
                "Conservar emergencia restante");
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(4)) == primero,
                "Conservar regular restante");
        comprobar(clinica.totalAbandonosRecursivo() == 0, "Cancelar no cuenta como abandono");
    }

    private static void probarExpiracion() {
        Clinica clinica = crearClinica();
        LocalDateTime noche = INICIO.withHour(23).withMinute(45);
        Paciente regular = paciente("R1", Paciente.Tipo.REGULAR, noche);
        Paciente otro = paciente("R2", Paciente.Tipo.REGULAR, noche);
        Paciente emergencia = paciente("E1", Paciente.Tipo.EMERGENCIA, noche);
        clinica.registrarPaciente("General", regular);
        clinica.registrarPaciente("General", emergencia);
        clinica.registrarPaciente("Pediatría", otro);
        clinica.expirarRegulares(noche.plusMinutes(30));
        comprobar(clinica.totalAbandonosRecursivo() == 0, "Límite de 30 minutos");
        clinica.expirarRegulares(noche.plusMinutes(30).plusSeconds(1));
        comprobar(clinica.totalAbandonosRecursivo() == 2, "Suma recursiva entre especialidades");
        comprobar(cantidadEnEspera(clinica, "General") == 1, "Retirar regular y conservar emergencia");
        comprobar(cantidadEnEspera(clinica, "Pediatría") == 0, "Expiración cruza medianoche");
        Especialidad area = clinica.getEspecialidades().buscarExterno("General").getDato();
        comprobar(area.getHistorialAbandonos().getDato() == regular, "Registrar quién abandonó");
        comprobar(area.getRegulares().isEmpty(), "Retirar también de la cola");
        clinica.expirarRegulares(noche.plusHours(2));
        comprobar(clinica.totalAbandonosRecursivo() == 2, "No contar dos veces un abandono");
        comprobar(clinica.getCancelaciones().isEmpty(), "Expiración no es cancelación manual");
        comprobar(clinica.atenderSiguiente("General", noche.plusHours(2)) == emergencia,
                "Las emergencias no expiran");

        clinica.registrarPaciente("General", paciente("R3", Paciente.Tipo.REGULAR, noche.plusHours(2)));
        clinica.registrarPaciente("Pediatría", paciente("E2", Paciente.Tipo.EMERGENCIA,
                noche.plusHours(2).plusMinutes(31)));
        comprobar(clinica.totalAbandonosRecursivo() == 3, "Registrar dispara expiración automática");
        comprobar(clinica.atenderSiguiente("Pediatría", noche.plusHours(3)).getId().equals("E2"),
                "Atención tras nueva inscripción");
        clinica.registrarPaciente("General", paciente("R4", Paciente.Tipo.REGULAR, noche.plusHours(3)));
        comprobar(clinica.atenderSiguiente("General", noche.plusHours(3).plusMinutes(31)) == null,
                "Atender descarta automáticamente los turnos vencidos");
        comprobar(clinica.totalAbandonosRecursivo() == 4, "Registrar expiración durante atención");
    }

    private static void probarExpiracionFueraDelFrente() {
        Clinica clinica = crearClinica();
        Paciente reciente = paciente("R1", Paciente.Tipo.REGULAR, INICIO.plusMinutes(20));
        Paciente antiguo = paciente("R2", Paciente.Tipo.REGULAR, INICIO);
        clinica.registrarPaciente("General", reciente);
        clinica.registrarPaciente("General", antiguo);
        Paciente siguiente = paciente("R3", Paciente.Tipo.REGULAR, INICIO.plusMinutes(21));
        clinica.registrarPaciente("General", siguiente);
        clinica.expirarRegulares(INICIO.plusMinutes(31));
        comprobar(clinica.totalAbandonosRecursivo() == 1, "Revisar también detrás del frente");
        comprobar(cantidadEnEspera(clinica, "General") == 2, "Eliminar nodo intermedio de sublista");
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(31)) == reciente,
                "Conservar paciente no expirado y orden de la cola");
        comprobar(clinica.atenderSiguiente("General", INICIO.plusMinutes(31)) == siguiente,
                "Conservar FIFO entre todos los sobrevivientes");
    }

    private static void probarCancelacionExpirada() {
        Clinica clinica = crearClinica();
        clinica.registrarPaciente("General", paciente("R1", Paciente.Tipo.REGULAR, INICIO));
        esperarError(() -> clinica.cancelarTurno("R1", INICIO.plusMinutes(31)));
        comprobar(clinica.totalAbandonosRecursivo() == 1, "Un turno vencido se registra como abandono");
        comprobar(clinica.getCancelaciones().isEmpty(), "Turno vencido no va a pila de cancelaciones");
        comprobar(cantidadEnEspera(clinica, "General") == 0, "Retirar turno vencido");
        clinica.registrarPaciente("General", paciente("R2", Paciente.Tipo.REGULAR, INICIO.plusMinutes(31)));
        clinica.cancelarTurno("R2", INICIO.plusMinutes(61));
        comprobar(clinica.getCancelaciones().top().getPaciente().getId().equals("R2"),
                "Cancelar a los 30 minutos exactos es manual");
    }

    private static void probarValidaciones() {
        Clinica clinica = new Clinica();
        comprobar(clinica.totalAbandonosRecursivo() == 0, "Caso base: clínica vacía");
        clinica.expirarRegulares(INICIO);
        esperarError(() -> clinica.agregarEspecialidad(null));
        esperarError(() -> clinica.agregarEspecialidad(" "));
        esperarError(() -> clinica.atenderSiguiente("No existe", INICIO));
        esperarError(() -> clinica.cancelarTurno("No existe", INICIO));
        esperarError(() -> clinica.expirarRegulares(null));
        esperarError(() -> new Paciente("", "Ana", Paciente.Tipo.REGULAR, INICIO));
        esperarError(() -> new Paciente("R1", null, Paciente.Tipo.REGULAR, INICIO));
        esperarError(() -> new Paciente("R1", "Ana", null, INICIO));
        esperarError(() -> new Paciente("R1", "Ana", Paciente.Tipo.REGULAR, null));
        clinica.agregarEspecialidad(" General ");
        clinica.agregarEspecialidad("Pediatría");
        esperarError(() -> clinica.agregarEspecialidad("General"));
        esperarError(() -> clinica.registrarPaciente("General", null));
        Paciente regular = paciente("R1", Paciente.Tipo.REGULAR, INICIO);
        esperarError(() -> clinica.registrarPaciente("No existe", regular));
        clinica.registrarPaciente("General", regular);
        esperarError(() -> clinica.registrarPaciente("Pediatría", regular));
        esperarError(() -> clinica.atenderSiguiente("General", INICIO.minusMinutes(1)));
        esperarError(() -> clinica.cancelarTurno("R1", INICIO.minusMinutes(1)));
        esperarError(() -> clinica.cancelarTurno("No existe", INICIO));
        comprobar(cantidadEnEspera(clinica, "General") == 1, "Errores preservan el turno válido");
        comprobar(cantidadEnEspera(clinica, "Pediatría") == 0, "ID único en toda la espera");
        comprobar(clinica.atenderSiguiente("General", INICIO) == regular, "Cola intacta tras errores");
    }

    private static Clinica crearClinica() {
        Clinica clinica = new Clinica();
        clinica.agregarEspecialidad("General");
        clinica.agregarEspecialidad("Pediatría");
        return clinica;
    }

    private static Paciente paciente(String id, Paciente.Tipo tipo, LocalDateTime registro) {
        return new Paciente(id, "Paciente " + id, tipo, registro);
    }

    private static int cantidadEnEspera(Clinica clinica, String especialidad) {
        int cantidad = 0;
        NodoInterno actual = clinica.getEspecialidades().obtenerListaInterna(especialidad);
        while (actual != null) {
            cantidad++;
            actual = actual.getSiguiente();
        }
        return cantidad;
    }

    private static void esperarError(Runnable operacion) {
        try {
            operacion.run();
        } catch (IllegalArgumentException esperado) {
            return;
        }
        throw new AssertionError("La operación debía rechazar la entrada");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
