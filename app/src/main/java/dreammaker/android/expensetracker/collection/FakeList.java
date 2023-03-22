package dreammaker.android.expensetracker.collection;

import java.util.AbstractList;

public class FakeList<I> extends AbstractList<I> {

    private final int mSize;

    public FakeList(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must be either 0 or a positive number");
        }
        this.mSize = size;
    }

    @Override
    public I get(int index) {
        if (index >= 0 && index < mSize) return null;
        throw new IndexOutOfBoundsException("index "+index+" size "+mSize);
    }

    @Override
    public int size() {
        return mSize;
    }
}
