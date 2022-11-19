package cn.bet.bhe.javaagent.bytebuddy;

import org.junit.jupiter.api.Test;

class FieldsAndMethodsTest {

    private final FieldsAndMethods fieldsAndMethods = new FieldsAndMethods();

    @Test
    void intercept() {
        try {
            fieldsAndMethods.intercept();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void interceptWithAnnotation() {
        try {
            fieldsAndMethods.interceptWithAnnotation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}