package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Slice<E> permits Slice.ArraySlice<E>, 
Slice.SubArraySlice<E>{
	
	static <V> Slice<V> array(V[] array){
		Objects.requireNonNull(array);
		return new ArraySlice<>(array);
	}
	
	static <W> Slice<W> array(W[] array, int from, int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new SubArraySlice<>(array, from, to);
	}
	
	Slice<E> subSlice(int from, int to);
	
	int size();
	E get(int index);

	
	final class ArraySlice<T> implements Slice<T> {
		
		private final T[] pointer;
		
		ArraySlice(T[] pointer) {
			Objects.requireNonNull(pointer);
			
			this.pointer = pointer;
		}
		
		public int size() {
			return pointer.length;
		}
		
		public T get(int index) {
			return pointer[index];
		}
		
		@Override
		public String toString() {
			return Arrays.stream(pointer)
					.map(e->e==null ? "null":e.toString())
					.collect(Collectors.joining(", ", "[", "]"));
		}

		public Slice<T> subSlice(int from, int to) {
			Objects.checkFromToIndex(from, to, size());
			return Slice.array(pointer, from, to);
		}
	}
	
	final class SubArraySlice<T> implements Slice<T> {
		
		private final T[] pointer;
		private final int from;
		private final int to;
		
		SubArraySlice(T[] pointer, int from, int to) {
			Objects.requireNonNull(pointer);
			
			this.pointer = pointer;
			this.from = from;
			this.to = to;
		}
		
		public int size() {
			return to - from;
		}
		
		public T get(int index) {
			Objects.checkIndex(index, size());
			return pointer[index + from];
		}
		
		@Override
		public String toString() {
			return Arrays.stream(pointer, from, to)
					.map(e->e==null ? "null":e.toString())
					.collect(Collectors.joining(", ", "[", "]"));
		}


		public Slice<T> subSlice(int from, int to) {
			Objects.checkFromToIndex(from, to, size());
			return Slice.array(pointer, from+this.from, to+this.from);
		}
	}
}
