package restaurant.orderingpage.dblogic;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DataStructure{
   
    //for linked list
    public static class LinkedList<T> implements Iterable<T> {
        private static class Node<T> {
            T data;
            Node<T> next;
            Node(T data) { this.data = data; }
        }

        private Node<T> head, tail;
        private int size;

        /** Adds to the end of the list. */
        public void add(T value) {
            Node<T> n = new Node<>(value);
            if (head == null) head = tail = n;
            else { tail.next = n; tail = n; }
            size++;
        }

        /** Adds to the front of the list. */
        public void addFirst(T value) {
            Node<T> n = new Node<>(value);
            n.next = head;
            head = n;
            if (tail == null) tail = n;
            size++;
        }

        public T get(int index) {
            if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index " + index);
            Node<T> cur = head;
            for (int i = 0; i < index; i++) cur = cur.next;
            return cur.data;
        }

        public T getFirst() {
            if (head == null) throw new NoSuchElementException("List is empty");
            return head.data;
        }

        public T removeFirst() {
            if (head == null) throw new NoSuchElementException("List is empty");
            T value = head.data;
            head = head.next;
            if (head == null) tail = null;
            size--;
            return value;
        }

        /** Removes the first node whose data equals the given value. */
        public boolean remove(T value) {
            Node<T> prev = null, cur = head;
            while (cur != null) {
                if (cur.data == value || (cur.data != null && cur.data.equals(value))) {
                    if (prev == null) head = cur.next; else prev.next = cur.next;
                    if (cur == tail) tail = prev;
                    size--;
                    return true;
                }
                prev = cur;
                cur = cur.next;
            }
            return false;
        }

        public boolean contains(T value) {
            for (Node<T> cur = head; cur != null; cur = cur.next)
                if (cur.data == value || (cur.data != null && cur.data.equals(value))) return true;
            return false;
        }

        public void clear() { head = tail = null; size = 0; }
        public int size() { return size; }
        public boolean isEmpty() { return size == 0; }

        /** Copies the elements into the given array (must be at least size() long). */
        public T[] toArray(T[] target) {
            int i = 0;
            for (Node<T> cur = head; cur != null; cur = cur.next) target[i++] = cur.data;
            return target;
        }

        @Override public Iterator<T> iterator() {
            return new Iterator<T>() {
                private Node<T> cur = head;
                @Override public boolean hasNext() { return cur != null; }
                @Override public T next() {
                    if (cur == null) throw new NoSuchElementException();
                    T v = cur.data; cur = cur.next; return v;
                }
            };
        }
    }
    
    //For queue
    public static class Queue<T> implements Iterable<T> {
        private final LinkedList<T> list = new LinkedList<>();

        public void enqueue(T value) { list.add(value); }
        public T dequeue() { return list.removeFirst(); }
        public T peek() { return list.getFirst(); }
        public boolean isEmpty() { return list.isEmpty(); }
        public int size() { return list.size(); }
        public void clear() { list.clear(); }

        @Override public Iterator<T> iterator() { return list.iterator(); }
    }

    //for stack
    public static class Stack<T> implements Iterable<T> {
        private final LinkedList<T> list = new LinkedList<>();

        public void push(T value) { list.addFirst(value); }
        public T pop() { return list.removeFirst(); }
        public T peek() { return list.getFirst(); }
        public boolean isEmpty() { return list.isEmpty(); }
        public int size() { return list.size(); }
        public void clear() { list.clear(); }

        @Override public Iterator<T> iterator() { return list.iterator(); }
    }
}
