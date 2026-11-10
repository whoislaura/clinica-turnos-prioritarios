public class PruebaEstructuras {
    public static void main(String[] args) {
        LinkedQueue<String> cola = new LinkedQueue<>();
        assert cola.poll() == null;
        cola.add("primero");
        cola.add("segundo");
        assert cola.poll().equals("primero");
        assert cola.poll().equals("segundo");
        assert cola.isEmpty();
        cola.add("tercero");
        assert cola.peek().equals("tercero");

        LinkedStack<String> pila = new LinkedStack<>();
        assert pila.pop() == null;
        pila.push("primero");
        pila.push("segundo");
        assert pila.pop().equals("segundo");
        assert pila.top().equals("primero");

        Multilista multilista = new Multilista();
        multilista.agregarNodoExterno(new Especialidad("Urgencias"));
        assert multilista.obtenerListaInterna("Urgencias") == null;
        assert multilista.obtenerListaInterna("Inexistente") == null;
        System.out.println("Estructuras básicas: OK");
    }
}
