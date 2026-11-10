public class LinkedQueue<E> {
    private Node<E> first;
    private Node<E> last;

    public boolean isEmpty() {
        return first == null;
    }

    public void add(E item) {
        Node<E> node = new Node<>(item);
        if (isEmpty()) {
            first = node;
        } else {
            last.setNext(node);
        }
        last = node;
    }

    public E peek() {
        return isEmpty() ? null : first.getInfo();
    }

    public E poll() {
        if (isEmpty()) {
            return null;
        }
        E item = first.getInfo();
        first = first.getNext();
        if (first == null) {
            last = null;
        }
        return item;
    }
}
