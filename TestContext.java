package org.salestrack.app.core.di;
public class TestContext {
    public static void check() {
        System.out.println(com.google.firebase.FirebaseApp.class.getMethods()[0].getParameterTypes()[0].getName());
    }
}
