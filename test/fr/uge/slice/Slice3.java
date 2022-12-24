package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public interface Slice3<E>{
	
	int size();
	E get(int index);
	
	
	static <V> Slice3<V> array(final V[] array){
		Objects.requireNonNull(array);
		return new Slice3<>() {
			
			public int size() {
				return array.length;
			}
			
			public V get(int index) {
				return array[index];
			}
			
			@Override
			public String toString() {
				return Arrays.stream(array) 
						.map(e->e==null ? "null":e.toString())
						.collect(Collectors.joining(", ", "[", "]"));
			}
		};
	}
	
	static <V> Slice3<V> array(final V[] array, final int from, final int to){
		Objects.requireNonNull(array);
		Objects.checkFromToIndex(from, to, array.length);
		return array(array).subSlice(from, to);
	}
	
	
	
	public default Slice3<E> subSlice(final int from, final int to) {
		Objects.checkFromToIndex(from, to, size());
		
		return new Slice3<>() {
			
			public int size() {
				return to - from;
			}
			
			public E get(int index) {
				Objects.checkIndex(index, size());
				return Slice3.this.get(index + from);
			}
			
			public String toString() {		
				var s = new StringJoiner(", ", "[", "]");
				IntStream.range(0, size()).forEach(i -> s.add(get(i) == null ? "null" : get(i).toString()));
				return s.toString();
			}
		};
	}
	
	
}
