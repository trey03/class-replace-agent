package com.itrey;

public class BTest extends ATest {
    @Override
    public String say(String text) {
        System.out.println("BTest:"+text);
        return "BBB";
    }
}
