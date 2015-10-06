package loglang;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * Created by skgchxngsxyz-osx on 15/10/06.
 */
public class InputStreamContextTest {
    private static int oldCapacity = 0;
    private static int oldReadSize = 0;


    private static InputStreamContext newInput(String text) {
        return new InputStreamContext("<dummy>", new ByteArrayInputStream(text.getBytes()));
    }

    @BeforeClass
    public static void setup() {
        oldCapacity = InputStreamContext.DEFAULT_CAPACITY;
        oldReadSize = InputStreamContext.DEFAULT_READ_SIZE;
    }

    @AfterClass
    public static void teardown() {
        InputStreamContext.DEFAULT_CAPACITY = oldCapacity;
        InputStreamContext.DEFAULT_READ_SIZE = oldReadSize;
    }

    @Before
    public void setupMethod() {
        InputStreamContext.DEFAULT_CAPACITY = oldCapacity;
        InputStreamContext.DEFAULT_READ_SIZE = oldReadSize;
    }

    @Test
    public void test() throws Exception {
        String text = "12";
        InputStreamContext input = newInput(text);

        assertEquals(text.length(), input.length());
        input.consume(2);

        assertEquals(input.getPosition(), input.length());
    }

    @Test
    public void test2() throws Exception {
        InputStreamContext.DEFAULT_CAPACITY = 4;
        InputStreamContext.DEFAULT_READ_SIZE = 2;

        String text = "0123456789";
        InputStreamContext input = newInput(text);

        assertEquals('0', input.byteAt(0));
        input.consume(1);

        assertEquals(2, input.length());
        assertEquals(2, input.length());

        assertEquals('1', input.byteAt(1));
        input.consume(1);

        assertEquals(4, input.length());
        assertEquals(4, input.length());

        input.rollback(0);
        for(int i = 0; i < text.length(); i++) {
            assertEquals(text.charAt(i), input.byteAt(i));
            input.consume(1);
        }
        assertEquals(text.length(), input.length());
    }
}