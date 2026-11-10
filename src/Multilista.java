public class Multilista {
    private NodoExterno cabeza;

    public NodoExterno getCabeza() {
        return cabeza;
    }

    public void agregarNodoExterno(Especialidad dato) {
        NodoExterno nuevo = new NodoExterno(dato);
        if (cabeza == null) {
            cabeza = nuevo;
            return;
        }
        NodoExterno actual = cabeza;
        while (actual.getSiguiente() != null) {
            actual = actual.getSiguiente();
        }
        actual.setSiguiente(nuevo);
    }

    public boolean agregarNodoInterno(String nombreEspecialidad, Paciente dato) {
        NodoExterno externo = buscarExterno(nombreEspecialidad);
        if (externo == null) {
            return false;
        }
        NodoInterno nuevo = new NodoInterno(dato);
        if (externo.getListaInterna() == null) {
            externo.setListaInterna(nuevo);
            return true;
        }
        NodoInterno actual = externo.getListaInterna();
        while (actual.getSiguiente() != null) {
            actual = actual.getSiguiente();
        }
        actual.setSiguiente(nuevo);
        return true;
    }

    public NodoInterno obtenerListaInterna(String nombreEspecialidad) {
        NodoExterno externo = buscarExterno(nombreEspecialidad);
        return externo == null ? null : externo.getListaInterna();
    }

    public Paciente eliminarNodoInterno(String nombreEspecialidad, String idPaciente) {
        NodoExterno externo = buscarExterno(nombreEspecialidad);
        if (externo == null) {
            return null;
        }
        NodoInterno anterior = null;
        NodoInterno actual = externo.getListaInterna();
        while (actual != null) {
            if (actual.getDato().getId().equals(idPaciente)) {
                if (anterior == null) {
                    externo.setListaInterna(actual.getSiguiente());
                } else {
                    anterior.setSiguiente(actual.getSiguiente());
                }
                return actual.getDato();
            }
            anterior = actual;
            actual = actual.getSiguiente();
        }
        return null;
    }

    public NodoExterno buscarExterno(String nombreEspecialidad) {
        NodoExterno actual = cabeza;
        while (actual != null) {
            if (actual.getDato().getNombre().equals(nombreEspecialidad)) {
                return actual;
            }
            actual = actual.getSiguiente();
        }
        return null;
    }
}
