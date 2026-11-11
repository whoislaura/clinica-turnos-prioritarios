import java.time.Duration;
import java.time.LocalDateTime;

public class Clinica {
    private static final Duration LIMITE_ESPERA = Duration.ofMinutes(30);
    private final Multilista especialidades = new Multilista();
    private final LinkedStack<Cancelacion> cancelaciones = new LinkedStack<>();

    public Multilista getEspecialidades() { return especialidades; }
    public LinkedStack<Cancelacion> getCancelaciones() { return cancelaciones; }

    public void agregarEspecialidad(String nombre) {
        validarTexto(nombre, "especialidad");
        if (especialidades.buscarExterno(nombre.strip()) != null) {
            throw new IllegalArgumentException("La especialidad ya existe");
        }
        especialidades.agregarNodoExterno(new Especialidad(nombre));
    }

    public void registrarPaciente(String especialidad, Paciente paciente) {
        NodoExterno externo = obtenerEspecialidad(especialidad);
        if (paciente == null) {
            throw new IllegalArgumentException("El paciente es obligatorio");
        }
        NodoExterno actual = especialidades.getCabeza();
        while (actual != null) {
            NodoInterno interno = actual.getListaInterna();
            while (interno != null) {
                Paciente enEspera = interno.getDato();
                if (enEspera.getId().equals(paciente.getId()) && !haExpirado(enEspera, paciente.getRegistro())) {
                    throw new IllegalArgumentException("El ID ya tiene un turno en espera");
                }
                interno = interno.getSiguiente();
            }
            actual = actual.getSiguiente();
        }
        expirarRegulares(paciente.getRegistro());
        especialidades.agregarNodoInterno(externo.getDato().getNombre(), paciente);
        colaDe(externo.getDato(), paciente).add(paciente);
    }

    public Paciente atenderSiguiente(String especialidad, LocalDateTime ahora) {
        NodoExterno externo = obtenerEspecialidad(especialidad);
        Especialidad area = externo.getDato();
        validarMomento(ahora);
        LinkedQueue<Paciente> cola = area.getEmergencias();
        Paciente primero = cola.peek();
        if (primero == null) {
            cola = area.getRegulares();
            primero = primerRegularVigente(externo.getListaInterna(), ahora);
        }
        if (primero != null && ahora.isBefore(primero.getRegistro())) {
            throw new IllegalArgumentException("La atención no puede ser anterior al registro");
        }
        expirarRegulares(ahora);
        Paciente paciente = cola.poll();
        if (paciente != null) {
            especialidades.eliminarNodoInterno(area.getNombre(), paciente.getId());
        }
        return paciente;
    }

    public void cancelarTurno(String idPaciente, LocalDateTime ahora) {
        validarTexto(idPaciente, "ID del paciente");
        validarMomento(ahora);
        String idBuscado = idPaciente.strip();
        NodoExterno externo = especialidades.getCabeza();
        while (externo != null) {
            NodoInterno interno = externo.getListaInterna();
            while (interno != null) {
                Paciente paciente = interno.getDato();
                if (paciente.getId().equals(idBuscado)) {
                    if (ahora.isBefore(paciente.getRegistro())) {
                        throw new IllegalArgumentException("La cancelación no puede ser anterior al registro");
                    }
                    boolean expirado = haExpirado(paciente, ahora);
                    expirarRegulares(ahora);
                    if (expirado) {
                        throw new IllegalArgumentException("El turno ya abandonó por tiempo excedido");
                    }
                    Especialidad area = externo.getDato();
                    Cancelacion cancelacion = new Cancelacion(paciente, area.getNombre(), ahora);
                    retirarDeCola(colaDe(area, paciente), paciente.getId());
                    especialidades.eliminarNodoInterno(area.getNombre(), paciente.getId());
                    cancelaciones.push(cancelacion);
                    return;
                }
                interno = interno.getSiguiente();
            }
            externo = externo.getSiguiente();
        }
        throw new IllegalArgumentException("El paciente no tiene un turno en espera");
    }

    public void expirarRegulares(LocalDateTime ahora) {
        // shortcut: expiración por eventos; añadir temporizador si se requiere expirar en reposo.
        validarMomento(ahora);
        NodoExterno externo = especialidades.getCabeza();
        while (externo != null) {
            Especialidad area = externo.getDato();
            LinkedQueue<Paciente> auxiliares = new LinkedQueue<>();
            while (!area.getRegulares().isEmpty()) {
                Paciente paciente = area.getRegulares().poll();
                if (haExpirado(paciente, ahora)) {
                    especialidades.eliminarNodoInterno(area.getNombre(), paciente.getId());
                    area.registrarAbandonoPorTiempo(paciente);
                } else {
                    auxiliares.add(paciente);
                }
            }
            while (!auxiliares.isEmpty()) {
                area.getRegulares().add(auxiliares.poll());
            }
            externo = externo.getSiguiente();
        }
    }

    public int totalAbandonosRecursivo() {
        return totalAbandonosRecursivo(especialidades.getCabeza());
    }

    private int totalAbandonosRecursivo(NodoExterno actual) {
        if (actual == null) {
            return 0;
        }
        return actual.getDato().getAbandonosPorTiempo()
                + totalAbandonosRecursivo(actual.getSiguiente());
    }

    private NodoExterno obtenerEspecialidad(String nombre) {
        validarTexto(nombre, "especialidad");
        NodoExterno externo = especialidades.buscarExterno(nombre.strip());
        if (externo == null) {
            throw new IllegalArgumentException("La especialidad no existe");
        }
        return externo;
    }

    private LinkedQueue<Paciente> colaDe(Especialidad area, Paciente paciente) {
        return paciente.getTipo() == Paciente.Tipo.EMERGENCIA
                ? area.getEmergencias() : area.getRegulares();
    }

    private boolean haExpirado(Paciente paciente, LocalDateTime ahora) {
        return paciente.getTipo() == Paciente.Tipo.REGULAR
                && Duration.between(paciente.getRegistro(), ahora).compareTo(LIMITE_ESPERA) > 0;
    }

    private Paciente primerRegularVigente(NodoInterno actual, LocalDateTime ahora) {
        while (actual != null) {
            Paciente paciente = actual.getDato();
            if (paciente.getTipo() == Paciente.Tipo.REGULAR && !haExpirado(paciente, ahora)) {
                return paciente;
            }
            actual = actual.getSiguiente();
        }
        return null;
    }

    private void retirarDeCola(LinkedQueue<Paciente> cola, String idPaciente) {
        LinkedQueue<Paciente> auxiliar = new LinkedQueue<>();
        while (!cola.isEmpty()) {
            Paciente paciente = cola.poll();
            if (!paciente.getId().equals(idPaciente)) {
                auxiliar.add(paciente);
            }
        }
        while (!auxiliar.isEmpty()) {
            cola.add(auxiliar.poll());
        }
    }

    private void validarTexto(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Debe indicar " + campo);
        }
    }

    private void validarMomento(LocalDateTime ahora) {
        if (ahora == null) {
            throw new IllegalArgumentException("Debe indicar la fecha y hora actuales");
        }
    }
}
