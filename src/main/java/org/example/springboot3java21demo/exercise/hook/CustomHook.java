package org.example.springboot3java21demo.exercise.hook;

class CustomHook implements EventHook {
    @Override
    public void execute() {
        System.out.println("Custom hook executed!");
    }
}
