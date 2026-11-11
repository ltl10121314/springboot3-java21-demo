package org.example.practice.jdk.core.hook;

class CustomHook implements EventHook {
    @Override
    public void execute() {
        System.out.println("Custom hook executed!");
    }
}
