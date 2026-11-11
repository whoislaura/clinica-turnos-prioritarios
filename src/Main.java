import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static LocalDateTime ahora;

    public static void main(String[] args) {
        Clinica clinica = new Clinica();
        ahora = LocalDateTime.now().withSecond(0).withNano(0);

        try (Scanner entrada = new Scanner(System.in)) {
            boolean salir = false;

            while (!salir) {
                mostrarMenu();
                try {
                    int opcion = leerEntero(entrada, "la opción");

                    switch (opcion) {
                        case 1:
                            agregarEspecialidad(clinica, entrada);
                            break;
                        case 2:
                            registrarPaciente(clinica, entrada);
                            break;
                        case 3:
                            mostrarEspera(clinica);
                            break;
                        case 4:
                            atenderSiguiente(clinica, entrada);
                            break;
                        case 5:
                            cancelarTurno(clinica, entrada);
                            break;
                        case 6:
                            mostrarCancelacion(clinica.getCancelaciones().top());
                            break;
                        case 7:
                            mostrarCancelaciones(clinica.getCancelaciones());
                            break;
                        case 8:
                            avanzarTiempo(clinica, entrada);
                            break;
                        case 9:
                            mostrarAbandonos(clinica);
                            break;
                        case 0:
                            salir = true;
                            System.out.println("Sesión finalizada.");
                            break;
                        default:
                            System.out.println("Opción inválida. Elija un número del 0 al 9.");
                    }
                } catch (NoSuchElementException error) {
                    System.out.println("\nFin de la entrada. Sesión finalizada.");
                    salir = true;
                } catch (IllegalArgumentException error) {
                    System.out.println("No se pudo completar la operación. " + error.getMessage());
                } catch (DateTimeException error) {
                    System.out.println("El reloj no admite esa fecha. Pruebe con menos minutos.");
                }
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\nCLÍNICA - Reloj simulado: " + ahora.format(FECHA));
        System.out.println("1. Agregar especialidad");
        System.out.println("2. Registrar paciente");
        System.out.println("3. Mostrar especialidades y pacientes en espera");
        System.out.println("4. Atender siguiente paciente");
        System.out.println("5. Cancelar turno");
        System.out.println("6. Consultar última cancelación");
        System.out.println("7. Consultar historial de cancelaciones");
        System.out.println("8. Avanzar minutos y procesar expiraciones");
        System.out.println("9. Consultar historial de abandonos y total recursivo");
        System.out.println("0. Salir");
        System.out.print("Escribe el número de una opción (0 a 9): ");
    }

    private static int leerEntero(Scanner entrada, String campo) {
        String texto = entrada.nextLine().strip();
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Ingrese un número entero válido para " + campo + ".", error);
        }
    }

    private static void agregarEspecialidad(Clinica clinica, Scanner entrada) {
        System.out.print("Nombre de la nueva especialidad (por ejemplo, Pediatría): ");
        String nombre = entrada.nextLine();
        clinica.agregarEspecialidad(nombre);
        System.out.println("Especialidad agregada.");
    }

    private static void registrarPaciente(Clinica clinica, Scanner entrada) {
        String especialidad = leerEspecialidad(clinica, entrada);
        System.out.print("ID único entre los turnos en espera (texto o número, por ejemplo, P001): ");
        String id = entrada.nextLine();
        System.out.print("Nombre del paciente (por ejemplo, Ana Pérez): ");
        String nombre = entrada.nextLine();
        System.out.print("Tipo de turno (escribe REGULAR o EMERGENCIA, en mayúsculas o minúsculas): ");
        String textoTipo = entrada.nextLine().strip();
        Paciente.Tipo tipo;
        if (textoTipo.equalsIgnoreCase("REGULAR")) {
            tipo = Paciente.Tipo.REGULAR;
        } else if (textoTipo.equalsIgnoreCase("EMERGENCIA")) {
            tipo = Paciente.Tipo.EMERGENCIA;
        } else {
            throw new IllegalArgumentException("El tipo debe ser REGULAR o EMERGENCIA.");
        }

        Paciente paciente = new Paciente(id, nombre, tipo, ahora);
        clinica.registrarPaciente(especialidad, paciente);
        System.out.println("Paciente registrado: " + describir(paciente));
    }

    private static void atenderSiguiente(Clinica clinica, Scanner entrada) {
        String especialidad = leerEspecialidad(clinica, entrada);
        Paciente paciente = clinica.atenderSiguiente(especialidad, ahora);
        if (paciente == null) {
            System.out.println("No hay pacientes en espera.");
        } else {
            System.out.println("Paciente atendido: " + describir(paciente));
        }
    }

    private static String leerEspecialidad(Clinica clinica, Scanner entrada) {
        NodoExterno actual = clinica.getEspecialidades().getCabeza();
        if (actual == null) {
            throw new IllegalArgumentException("Primero agrega una especialidad con la opción 1.");
        }

        System.out.println("Especialidades disponibles:");
        while (actual != null) {
            System.out.println("  - " + actual.getDato().getNombre());
            actual = actual.getSiguiente();
        }
        System.out.print("Escribe el nombre tal como aparece en la lista: ");
        return entrada.nextLine();
    }

    private static void cancelarTurno(Clinica clinica, Scanner entrada) {
        System.out.print("ID del turno en espera (igual que en la opción 3, por ejemplo, P001): ");
        String id = entrada.nextLine();
        clinica.cancelarTurno(id, ahora);
        System.out.println("Turno cancelado y registrado en el historial.");
    }

    private static void avanzarTiempo(Clinica clinica, Scanner entrada) {
        System.out.print("Minutos que deseas avanzar (entero positivo, por ejemplo, 15): ");
        int minutos = leerEntero(entrada, "los minutos");
        if (minutos <= 0) {
            throw new IllegalArgumentException("Los minutos deben ser mayores que cero.");
        }
        LocalDateTime nuevaHora = ahora.plusMinutes(minutos);
        int anteriores = clinica.totalAbandonosRecursivo();
        clinica.expirarRegulares(nuevaHora);
        ahora = nuevaHora;
        System.out.println("Reloj actualizado: " + ahora.format(FECHA));
        System.out.println("Nuevos abandonos por tiempo excedido: "
                + (clinica.totalAbandonosRecursivo() - anteriores));
    }

    private static String describir(Paciente paciente) {
        return "[" + paciente.getId() + "] " + paciente.getNombre() + " - " + paciente.getTipo()
                + " - registro: " + paciente.getRegistro().format(FECHA);
    }

    private static void mostrarEspera(Clinica clinica) {
        NodoExterno externo = clinica.getEspecialidades().getCabeza();
        if (externo == null) {
            System.out.println("No hay especialidades registradas.");
        }
        while (externo != null) {
            System.out.println("Especialidad: " + externo.getDato().getNombre());
            if (externo.getListaInterna() == null) {
                System.out.println("  No hay pacientes en espera.");
            } else {
                mostrarPacientes(externo.getListaInterna(), Paciente.Tipo.EMERGENCIA);
                mostrarPacientes(externo.getListaInterna(), Paciente.Tipo.REGULAR);
            }
            externo = externo.getSiguiente();
        }
    }

    private static void mostrarPacientes(NodoInterno actual, Paciente.Tipo tipo) {
        while (actual != null) {
            if (actual.getDato().getTipo() == tipo) {
                System.out.println("  " + describir(actual.getDato()));
            }
            actual = actual.getSiguiente();
        }
    }

    private static void mostrarCancelacion(Cancelacion cancelacion) {
        if (cancelacion == null) {
            System.out.println("No hay cancelaciones registradas.");
            return;
        }
        System.out.println("Cancelación: " + describir(cancelacion.getPaciente())
                + " - especialidad: " + cancelacion.getEspecialidad()
                + " - cancelado: " + cancelacion.getMomento().format(FECHA));
    }

    private static void mostrarCancelaciones(LinkedStack<Cancelacion> pila) {
        if (pila.isEmpty()) {
            System.out.println("No hay cancelaciones registradas.");
            return;
        }
        LinkedStack<Cancelacion> auxiliar = new LinkedStack<>();
        try {
            while (!pila.isEmpty()) {
                Cancelacion cancelacion = pila.pop();
                auxiliar.push(cancelacion);
                mostrarCancelacion(cancelacion);
            }
        } finally {
            while (!auxiliar.isEmpty()) {
                pila.push(auxiliar.pop());
            }
        }
    }

    private static void mostrarAbandonos(Clinica clinica) {
        NodoExterno externo = clinica.getEspecialidades().getCabeza();
        while (externo != null) {
            System.out.println("Especialidad: " + externo.getDato().getNombre());
            NodoInterno actual = externo.getDato().getHistorialAbandonos();
            if (actual == null) {
                System.out.println("  No hay abandonos por tiempo excedido.");
            }
            while (actual != null) {
                System.out.println("  " + describir(actual.getDato()) + " - abandonado por tiempo excedido");
                actual = actual.getSiguiente();
            }
            externo = externo.getSiguiente();
        }
        System.out.println("Total recursivo de abandonos: " + clinica.totalAbandonosRecursivo());
    }
}
