package com.itrey;

public class CTest {
    private ATest aaTest = new ATest();
    public void test(){
        ATest aTest = new ATest();
        aTest.say("aaa");
    }
    public void test2(){
        aaTest.say("aaa2");
    }
}
