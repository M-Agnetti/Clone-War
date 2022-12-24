## SAIDI Soumia
## Groupe n°2

# TP3 - Slices of bread


Le but de ce TP est d'écrire 4 versions différentes du concept de slice (une vue partielle d'un tableau) pour comprendre les notions de classe interne et de classe anonyme en Java.


# Exercice 2 - The Slice and The furious

Un slice est une structure de données qui permet de "virtuellement" découper un tableau en gardant des indices de début et de fin (from et to) ainsi qu'un pointeur sur le tableau. Cela évite de recopier tous les éléments du tableau, c'est donc beaucoup plus efficace.
Le concept d'array slicing est un concept très classique dans les langages de programmation, même si chaque langage vient souvent avec une implantation différente.

Les tests JUnit 5 de cet exercice sont SliceTest.java. 

  1.  
        On va dans un premier temps créer une interface Slice avec une méthode array qui permet de créer un slice à partir d'un tableau en Java.

        ```java
        String[] array = new String[] { "foo", "bar" };
        Slice<String> slice = Slice.array(array);
        ```
     
        L'interface Slice est paramétrée par le type des éléments du tableau et permet que les éléments soient null.
        L'interface Slice possède deux méthodes d'instance, size qui renvoie le nombre d'éléments et get(index) qui renvoie le index-ième (à partir de zéro).
        En termes d'implantation, on va créer une classe interne à l'interface Slice nommée ArraySlice implantant l'interface Slice. L'implantation ne doit pas recopier les valeurs du tableau donc un changement d'une des cases du tableau doit aussi être visible si on utilise la méthode get(index).
        Implanter la classe Slice et les méthodes array, size et get(index).
        Vérifier que les tests JUnit marqués "Q1" passent.
        </br>
        ***Réponse :***

        ```
        La classe ArraySlice doit être final car l'interface est scellée.
        ```

        ```java
        public sealed interface Slice<E> permits Slice.ArraySlice<E>{
        
            static <V> Slice<V> array(V[] array){
                Objects.requireNonNull(array);
                return new ArraySlice<>(array);
            }
            
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
                    Objects.checkIndex(index, size());
                    return pointer[index];
                }
                
            }

        }
        ```

  2.  
        On souhaite que l'affichage d'un slice affiche les valeurs séparées par des virgules avec un '[' et un ']' comme préfixe et suffixe.
        Par exemple,
            var array = new String[] { "foo", "bar" };
            var slice = Slice.array(array);
            System.out.println(slice);   // [foo, bar]
            
        En terme d'implantation, penser à utiliser un Stream avec le bon Collector !
        Vérifier que les tests JUnit marqués "Q2" passent.
        </br>
        ***Réponse :***

        ```
        On utilise le Collector qui prend trois paramètres dont un préfixe et un suffixe.
        ```

        ```java
        public sealed interface Slice<E> permits Slice.ArraySlice<E>{
        
            @Override
            public String toString() {
                return Arrays.stream(pointer)
                        .map(e->e==null ? "null":e.toString())
                        .collect(Collectors.joining(", ", "[", "]"));
            }

        }
        ```

  3.  
        On souhaite ajouter une surcharge à la méthode array qui, en plus de prendre le tableau en paramètre, prend deux indices from et to et montre les éléments du tableau entre from inclus et to exclus.
        Par exemple
        ```java
        String[] array = new String[] { "foo", "bar", "baz", "whizz" };
        Slice<String> slice = Slice.array(array, 1, 3);
        ```
        En terme d'implantation, on va créer une autre classe interne nommée SubArraySlice implantant l'interface Slice.
        Vérifier que les tests JUnit marqués "Q3" passent.
        Note : il existe une méthode Arrays.stream(array, from, to) dans la classe java.util.Arrays

        </br> 

        ***Réponse :***


        ```java
        public sealed interface Slice<E> permits Slice.ArraySlice<E>, 
            Slice.SubArraySlice<E>{
                
                ...
                
                static <V> Slice<V> array(V[] array, int from, int to){
                    Objects.requireNonNull(array);
                    Objects.checkFromToIndex(from, to, array.length);
                    return new SubArraySlice<>(array, from, to);
                }
                
                ...
                
                final class ArraySlice<T> implements Slice<T> {
                    ...
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
                }
            }
        ```


  4.  
        On souhaite enfin ajouter une méthode subSlice(from, to) à l'interface Slice qui renvoie un sous-slice restreint aux valeurs entre from inclus et to exclu.
        Par exemple,
                String[] array = new String[] { "foo", "bar", "baz", "whizz" };
                Slice<String> slice = Slice.array(array);
                Slice<String> slice2 = slice.subSlice(1, 3);
            

        Bien sûr, cela veut dire implanter la méthode subSlice(from, to) dans les classes ArraySlice et SubArraySlice.
        Vérifier que les tests JUnit marqués "Q4" passent.
      
        </br>

        ***Réponse :***

        ```java
        public sealed interface Slice<E> permits Slice.ArraySlice<E>, 
Slice.SubArraySlice<E>{
	
            ...
            
            Slice<E> subSlice(int from, int to);
            
            int size();
            E get(int index);

            
            final class ArraySlice<T> implements Slice<T> {
                
                ...

                public Slice<T> subSlice(int from, int to) {
                    Objects.checkFromToIndex(from, to, size());
                    return Slice.array(pointer, from, to);
                }
            }
            
            final class SubArraySlice<T> implements Slice<T> {
                
                ...

                public Slice<T> subSlice(int from, int to) {
                    Objects.checkFromToIndex(from, to, size());
                    return Slice.array(pointer, from+this.from, to+this.from);
                }
            }
        }
        ```


# Exercice 3 - 2 Slice 2 Furious

Le but de cet exercice est d'implanter l'interface Slice2 qui possède les mêmes méthodes que l'interface Slice mais on souhaite de la classe SubArraySlice soit une inner class de la classe ArraySlice.

Les tests JUnit 5 de cet exercice sont Slice2Test.java.


  1.  
        Recopier l'interface Slice de l'exercice précédent dans une interface Slice2. Vous pouvez faire un copier-coller de Slice dans même package, votre IDE devrait vous proposer de renommer la copie. Puis supprimer la classe interne SubArraySlice ainsi que la méthode array(array, from, to) car nous allons les réimplanter et commenter la méthode subSlice(from, to) de l'interface, car nous allons la ré-implanter aussi, mais plus tard.
        Vérifier que les tests JUnit marqués "Q1" et "Q2" passent.
      
        </br>
        
        ***Réponse :***

        ```java
        public sealed interface Slice2<E> permits Slice2.ArraySlice<E>{
        
            static <V> Slice2<V> array(V[] array){
                Objects.requireNonNull(array);
                return new ArraySlice<>(array);
            }
            
            //Slice2<E> subSlice(int from, int to);
            
            int size();
            E get(int index);

            
            final class ArraySlice<T> implements Slice2<T> {
                
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

                /*public Slice2<T> subSlice(int from, int to) {
                    Objects.checkFromToIndex(from, to, size());
                    return Slice2.array(pointer, from, to);
                }
                */
            }
            
        }
        ```

  2.  
        Déclarer une classe SubArraySlice à l'intérieur de la classe ArraySlice comme une inner class donc pas comme une classe statique et implanter cette classe et la méthode array(array, from, to).
        Vérifier que les tests JUnit marqués "Q3" passent.
        </br>
        ***Réponse :***

        ```java
        public sealed interface Slice2<E> permits Slice2.ArraySlice<E>, Slice2.ArraySlice<E>.SubArraySlice{
	
            ...
            
            static <V> Slice2<V> array(V[] array, int from, int to){
                Objects.requireNonNull(array);
                Objects.checkFromToIndex(from, to, array.length);
                
                return new ArraySlice<V>(array).new SubArraySlice(from, to);
            }
                    
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
                    Objects.checkIndex(index, size());
                    return pointer[index];
                }
                
                @Override
                public String toString() {
                    return Arrays.stream(pointer)
                            .map(e->e==null ? "null":e.toString())
                            .collect(Collectors.joining(", ", "[", "]"));
                }	
                
            }
            
        }
        ```

  3.  
        Dé-commenter la méthode subSlice(from, to) de l'interface et fournissez une implantation de cette méthode dans les classes ArraySlice et SubArraySlice.
        On peut noter qu'il est désormais possible de simplifier le code de array(array, from, to).
        Vérifier que les tests JUnit marqués "Q4" passent.
        </br> 

        ***Réponse :***
        ```java
        public sealed interface Slice2<E> permits Slice2.ArraySlice<E>, Slice2.ArraySlice<E>.SubArraySlice{
	
            static <V> Slice2<V> array(V[] array, int from, int to){
                Objects.requireNonNull(array);
                Objects.checkFromToIndex(from, to, array.length);
                return new ArraySlice<V>(array).subSlice(from, to);
            }
            
            Slice2<E> subSlice(int from, int to);
            
            final class ArraySlice<T> implements Slice2<T> {
                final class SubArraySlice implements Slice2<T>{
                    
                    ...

                    public Slice2<T> subSlice(int from, int to) {
                        Objects.checkFromToIndex(from, to, size());
                        return new SubArraySlice(from + this.from, to + this.from);
                    }
                }
                
                private final T[] pointer;
                
                ...

                public Slice2<T> subSlice(int from, int to) {
                    Objects.checkFromToIndex(from, to, size());
                    return new SubArraySlice(from, to);
                }
            }
        }
        ```


  4.  
      Dans quel cas va-t-on utiliser une inner class plutôt qu'une classe interne ?

      ***Réponse :***

      ```
      On utilisera une classe interne si elle n'est pas une sous implantation d'une autre classe. On utilisera donc plutôt une inner class dans le cas de partage des données / champs (si on pense à de l'héritage) où une classe est une sous implantation pour pouvoir faire de la délégation.
      ```

# Exercice 4 - The Slice and The Furious: Tokyo Drift

    Le but de cet exercice est d'implanter l'interface Slice3 qui possède les mêmes méthodes que l'interface Slice mais on va transformer les classes ArraySlice et SubArraySlice en classes anonymes.

    Les tests JUnit 5 de cet exercice sont Slice3Test.java.

  1.  
        Recopier l'interface Slice du premier exercice dans une interface Slice3. Supprimer la classe interne SubArraySlice ainsi que la méthode array(array, from, to) car nous allons les réimplanter et commenter la méthode subSlice(from, to) de l'interface, car nous allons la réimplanter plus tard.
        Puis déplacer la classe ArraySlice à l'intérieur de la méthode array(array) et transformer celle-ci en classe anonyme.
        Vérifier que les tests JUnit marqués "Q1" et "Q2" passent.

      
        </br>
        
        ***Réponse :***

        ```java
        public interface Slice3<E>{
	
            //Slice3<E> subSlice(int from, int to);
            
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
            
            
            
        }
        ```

  2.  
        On va maintenant chercher à implanter la méthode subSlice(from, to) directement dans l'interface Slice3. Ainsi, l'implantation sera partagée.
        Écrire la méthode subSlice(from, to) en utilisant là encore une classe anonyme.
        Comme l'implantation est dans l'interface, on n'a pas accès au tableau qui n'existe que dans l'implantation donnée dans la méthode array(array)... mais ce n'est pas grave, car on peut utiliser les méthodes de l'interface.
        Puis fournissez une implantation à la méthode array(array, from, to).
        Vérifier que les tests JUnit marqués "Q3" et "Q4" passent.
      
        </br>

        ***Réponse :***

        ```java
        public interface Slice3<E>{
	            
            int size();
            E get(int index);
            
            static <V> Slice3<V> array(final V[] array){
                ...
            }
            
            static <V> Slice3<V> array(final V[] array, final int from,final int to){
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
        ```

  3.  
      Dans quel cas va-t-on utiliser une classe anonyme plutôt qu'une classe interne ?

     </br> 

      ***Réponse :***

      ```
      On va plutôt utiliser une classe anonyme dans le cas où ne l'on souhaite pas montrer l'implantation ou alors dans le cas où la classe ne sera pas utilisée en tant que telle (ici, par exemple, on appelle les Slices une seule fois dans la méthode statique array et c'est array qui est appelée à l'extérieur).
      ```



# Exercice 5 - Slice & Furious (optionel)

    Le but de cet exercice est d'implanter l'interface Slice4 qui possède les mêmes méthodes que l'interface Slice mais au lieu d'avoir deux implantations, on va simplifier les choses en ayant une seule implantation.
    De plus, au lieu d'utiliser une classe interne, une inner class ou une classe anonyme, on va utiliser une feature assez méconnue de Java : on peut avoir plusieurs classes/interfaces les unes derrières les autres dans un même fichier en Java, pourvu qu'une seule classe/interface soit publique.

    Les tests JUnit 5 de cet exercice sont Slice4Test.java.

  1.  
        Déclarer l'interface Slice4 avec les méthodes size, get(index) et subSlice(from, to) abstraites. De plus, la méthode array(array) peut déléguer son implantation à la méthode array(array, from, to).
        Pour l'instant, commenter la méthode subSlice(from, to) que l'on implantera plus tard.
        À la suite du fichier, déclarer une classe non publique SliceImpl implantant l'interface Slice4 et implanter la méthode array(array, from, to).
        Vérifier que les tests JUnit marqués "Q1", "Q2" et "Q3" passent.

        </br>

        ***Réponse :***

        ```java
        public interface Slice4<E> {
	
            int size();
            E get(int index);
            //Slice4<E> subSlice(int from, int to);
            
            
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
        ```
        ```java
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
            
        }
        ```

  2.     
        Dé-commenter la méthode subSlice(from, to) et fournissez une implantation de cette méthode.
        Vérifier que les tests JUnit marqués "Q4" passent.

        </br>

        ***Réponse :***

        ```java
        public interface Slice4<E> {
	
            int size();
            E get(int index);
            Slice4<E> subSlice(int from, int to);
            
            ...
            
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
            
            ...

            public Slice4<U> subSlice(int from, int to) {
                Objects.checkFromToIndex(from, to, size());
                return Slice4.array(pointer, from + this.from, to + this.from);
            }
            
        }
        ```


  3.  
        On peut remarquer qu'en programmation objet il y a une toujours une tension entre avoir une seule classe et donc avoir des champs qui ne servent pas vraiment pour certaines instances et avoir plusieurs classes ayant des codes très similaires, mais avec un nombre de champs différents.
        L'orthodoxie de la POO voudrait que l'on ait juste le nombre de champs qu'il faut, en pratique, on a tendance à ne pas créer trop de classes, car plus on a de code plus c'est difficile de le faire évoluer.
        À votre avis, pour cet exemple, est-il préférable d'avoir deux classes une pour les tableaux et une pour les tableaux avec des bornes ou une seule classe gérant les deux cas ?

        </br> 

        ***Réponse :***

        ```
        Puisque les tableaux avec des bornes peuvent être considérés comme un sous-cas des tableaux, il est, je pense, préférable d'avoir une seule classe gérant les 2 cas utilisant notamment la seconde implémentation pour implémenter la première et donc permettre de factoriser pas mal de code.
        ```

