public class NewTest {
    private final int test;
    
    public NewTest(int test){
        this.test = test;
    }
    
    public static void main(String[] args) {
        var nt = new NewTest(3);
        System.out.println(nt.test);
    }
}
