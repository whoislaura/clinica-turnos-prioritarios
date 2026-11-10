public class LinkedStack<E> {
    private Node<E> top;

    public boolean isEmpty() {
        return top == null;
    }

    public void push(E item) {
        top = new Node<>(item, top);
    }

    public E top() {
        return isEmpty() ? null : top.getInfo();
    }

    public E pop() {
        if (isEmpty()) {
            return null;
        }
        E item = top.getInfo();
        top = top.getNext();
        return item;
    }
}
