package mod.jesroads2.util;

public class LimitedStack<T> {
    private final Object[] stack;

    private final int maxSize;
    private int size, index;

    public LimitedStack() {
        this(30);
    }

    public LimitedStack(int max) {
        this.stack = new Object[max];
        this.maxSize = max;
        this.clear();
    }

    public void clear() {
        this.size = 0;
        this.index = 0;
    }

    public int size() {
        return this.size;
    }

    public int maxSize() {
        return this.maxSize;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public void push(T element) {
        this.stack[this.index] = element;
        this.incrementIndex();
        if (this.size < this.maxSize) this.size++;
    }

    public T pop() {
        if (this.isEmpty()) return null;

        this.decrementIndex();
        this.size--;
        return (T) this.stack[this.index];
    }

    private void incrementIndex() {
        int newIndex = this.index + 1;
        if (newIndex < this.maxSize) this.index = newIndex;
        else this.index = 0;
    }

    private void decrementIndex() {
        int newIndex = this.index - 1;
        if (newIndex >= 0) this.index = newIndex;
        else this.index = this.maxSize - 1;
    }
}