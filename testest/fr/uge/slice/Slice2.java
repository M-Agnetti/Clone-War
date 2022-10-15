package fr.uge.slice;

import java.util.Arrays; 
import java.util.Objects;
import java.util.stream.Collectors;

public sealed interface Slice2<E> permits Slice2.ArraySlice<E>, Slice2.ArraySlice<E>.SubArraySlice{
	
	static <V> Slice2<V> array(V[] array){
		Objects.requireNonNull(array);
		return new ArraySlice<>(array);
	}
	
	static <V> Slice2<V> array(V[] array, int from, int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return new ArraySlice<V>(array).subSlice(from, to);
	}
	
	Slice2<E> subSlice(int from, int to);
	
	int size();
	E get(int index);

	
	final class ArraySlice<T> implements Slice2<T> {
		final class SubArraySlice implements Slice2<T>{
			
			private final int from;
			private final int to;
			
			private SubArraySlice(int from, int to) {
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
			
			public Slice2<T> subSlice(int from, int to) {
				Objects.checkFromToIndex(from, to, size());
				return new SubArraySlice(from + this.from, to + this.from);
			}
			
		}
		
		private final T[] pointer;
		
		private ArraySlice(T[] pointer) {
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

		public Slice2<T> subSlice(int from, int to) {
			Objects.checkFromToIndex(from, to, size());
			return new SubArraySlice(from, to );
		}
		
		
	}
	
	
}
