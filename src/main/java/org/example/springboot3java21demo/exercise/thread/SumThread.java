package org.example.springboot3java21demo.exercise.thread;

public class SumThread extends Thread {
    private int start;
    private int end;
    private int[] result;

    public SumThread(int start, int end, int[] result) {
        this.start = start;
        this.end = end;
        this.result = result;
    }

    @Override
    public void run() {
        int sum = 0;
        for (int i = start; i <= end; i++) {
            sum += i;
        }
        synchronized (result) {
            result[0] += sum; // Update the result array's first element with the partial sum
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int[] result = new int[1]; // To hold the final sum
        int n = 100; // Number of elements to sum up to
        int numThreads = 4; // Number of threads to use
        int chunkSize = n / numThreads; // Size of each chunk of numbers to process by each thread

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            int start = (i * chunkSize) + 1;
            int end = (i == numThreads - 1) ? n : (i + 1) * chunkSize; // Ensure the last thread processes to n
            threads[i] = new SumThread(start, end, result);
            threads[i].start(); // Start the thread
        }

        for (Thread t : threads) {
            t.join(); // Wait for all threads to complete
        }

        System.out.println("Sum from 1 to " + n + " is: " + result[0]); // Print the final sum
    }
}
