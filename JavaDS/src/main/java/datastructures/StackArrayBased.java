package datastructures;

/**
 * Create an array based stack implementation. you have the option making it
 * bounded or unbounded.
 * 
 * @author abhinav
 *
 * @param <T>
 */
public class StackArrayBased<T> {
    private int top = -1;
    private int initCapacity;
    private boolean bounded;
    private T array[];
    private boolean stackOverflow;

    @SuppressWarnings("unchecked")
    /**
     * 
     * @param bounded
     *            is the stack capacity fixed.
     * @param initCapacity
     *            initial capacity.
     */
    public StackArrayBased(boolean bounded, int initCapacity) {
	this.bounded = bounded;
	this.initCapacity = initCapacity;
	array = (T[]) new Object[initCapacity];
    }

    public boolean isEmpty() {
	return top == -1;
    }

    public int getSize() {
	return top + 1;
    }

    public boolean push(T t) {
	if (top == (initCapacity - 1)) {
	    if (bounded) {
		System.err.println("Stack overflow error!!");
		stackOverflow = true;
		return false;
	    } else {
		T[] newArr = (T[]) new Object[initCapacity * 2];
		System.arraycopy(array, 0, newArr, 0, initCapacity);
		array = newArr;
		initCapacity = initCapacity * 2;

	    }
	}
	top++;
	array[top] = t;
	return true;

    }

    public T pop() {
	if (top == -1)
	    throw new IllegalStateException("Stack is empty");
	T retVal = array[top];
	top--;
	return retVal;
    }

    @Override
    public String toString() {
	StringBuffer bf = new StringBuffer("");
	for (T t : array) {
	    if (t == null)
		break;
	    bf.append(t + ",");
	}
	bf.replace(bf.length() - 1, bf.length(), "");
	return bf.toString();
    }

    public boolean isStackOverflow() {
	return stackOverflow;
    }

}
