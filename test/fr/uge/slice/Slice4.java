package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public interface Slice4<E> {
	
	int size();
	E get(int index);
	Slice4<E> subSlice(int from, int to);
	
	
	public static <T> Slice4<T> array(final T[] array){
		Objects.requireNonNull(array);
		return array(array, 0, array.length);
	}
	
	public static <T> Slice4<T> array(final T[] array, final int from, final int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new SliceImpl<>(array, from, to);
	}

}

class SliceImpl<U> implements Slice4<U> {
	
	private final U[] pointer;
	private final int from;
	private final int to;
	
	SliceImpl(U[] pointer, int from, int to) {
		Objects.requireNonNull(pointer);
		
		this.pointer = pointer;
		this.from = from;
		this.to = to;
	}
	
	public int size() {
		return to - from;
	}
	
	public U get(int index) {
		Objects.checkIndex(index, size());
		return pointer[index + from];
	}
	
	@Override
	public String toString() {
		return Arrays.stream(pointer, from, to)
				.map(e->e==null ? "null":e.toString())
				.collect(Collectors.joining(", ", "[", "]"));
	}

	public Slice4<U> subSlice(int from, int to) {
		Objects.checkFromToIndex(from, to, size());
		return Slice4.array(pointer, from + this.from, to + this.from);
	}
	
}
