package fr.uge.main;


public class Main {
	
private final int x;
	
	public Main(int x) {
		this.x = x;
	}
	
	/*public class classe {
		
		private final int n = 0;
	}
	*/
	
	
	private final Person2 p = new Person2(16,"Robert");
	private final int ii = 0;
	/*
	private static void testFunction() {
		System.out.println("hey j'imprime un truc");
	}
*/
	
	public static void main(String[] args) {
		for(var i = 0;
				i<10;
				i++) 
		{
			System.out.println(i);
		}
		/*
		var pp = new Person("Bob", 16);
		
		var m = new Main();
		System.out.println(m.n + " ");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}

}
