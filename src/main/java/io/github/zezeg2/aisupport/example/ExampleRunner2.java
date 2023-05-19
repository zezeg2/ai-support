package io.github.zezeg2.aisupport.example;

public class ExampleRunner2 {
    public static void main(String[] args) {
        Class<?> integerClass = Integer.class;
        Class<?> booleanClass = Boolean.class;
        Class<?> StringClass = String.class;
        Class<?> intClass = int.class;

        System.out.println("Is Integer a wrapper class? " + isStringOrWrapperClass(integerClass)); // Output: true
        System.out.println("Is Boolean a wrapper class? " + isStringOrWrapperClass(booleanClass)); // Output: true
        System.out.println("Is String a wrapper class? " + isStringOrWrapperClass(StringClass)); // Output: false
        System.out.println("Is int a wrapper class? " + isStringOrWrapperClass(intClass)); // Output: false
        System.out.println(intClass.getSimpleName());
    }

    public static boolean isStringOrWrapperClass(Class<?> type) {
        return type == Boolean.class || type == Character.class || type == Byte.class ||
                type == Short.class || type == Integer.class || type == Long.class || type == Float.class ||
                type == Double.class || type == String.class;
    }
}
