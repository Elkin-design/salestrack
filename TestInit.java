import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
public class TestInit {
    public static void main(String[] args) {
        System.out.println("Methods:");
        for(java.lang.reflect.Method m : FirebaseApp.class.getMethods()) {
            if(m.getName().equals("initializeApp")) {
                System.out.print(m.getName() + "(");
                for(Class<?> pt : m.getParameterTypes()) {
                    System.out.print(pt.getName() + " ");
                }
                System.out.println(")");
            }
        }
    }
}
