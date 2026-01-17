package org.example.springboot3java21demo.exercise.hook;

public class MyEvent extends Event {

    private EventHook eventHook;

    public MyEvent(EventHook eventHook) {
        this.eventHook = eventHook;
    }

    @Override
    public void trigger() {
        System.out.println("Event is triggered!");
        if (eventHook != null) {
            eventHook.execute();
        }
    }
}
