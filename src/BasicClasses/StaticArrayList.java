package BasicClasses;

import java.util.ArrayList;
import java.util.Collection;

public class StaticArrayList<E> extends ArrayList<E> {

    private final int max_size;

    public StaticArrayList(int size){
        super(size);
        this.max_size = size;
    }

    public StaticArrayList(Collection<E> collection){
        super(collection.size());
        this.max_size = collection.size();
        this.addAll(collection);
    }

    public StaticArrayList(E[] collection){
        super(collection.length);
        this.max_size = collection.length;
        this.addAll(collection);
    }

    public boolean add(E elem){
        if(super.size() < max_size) {
            super.add(elem);
            return true;
        }
        else
            throw new ArrayIndexOutOfBoundsException();
    }

    public boolean addAll(E[] a){
        boolean modified = false;
        for(E e : a)
            if(add(e))
                modified = true;

        return modified;
    }
}