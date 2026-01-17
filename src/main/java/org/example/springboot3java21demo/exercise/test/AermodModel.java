package org.example.springboot3java21demo.exercise.test;

/**
 * aermodModel
 */
public class AermodModel {

    public static void main(String[] args) {
        // Input parameters
        double emissionRate = 100.0; // Emission rate in kg/hr
        double stackHeight = 50.0; // Stack height in meters
        double windSpeed = 5.0; // Wind speed in m/s
        double distance = 1000.0; // Distance from the source in meters

        // Calculate dispersion factor
        double dispersionFactor = calculateDispersionFactor(emissionRate, stackHeight, windSpeed, distance);

        // Print the result
        System.out.println("Dispersion Factor: " + dispersionFactor);
    }

    private static double calculateDispersionFactor(double emissionRate, double stackHeight, double windSpeed, double distance) {
        // Constants
        double sigmaY = 0.07 * Math.pow(distance, 0.6);
        double sigmaZ = 0.10 * Math.pow(distance, 0.6);
        double sigmaX = 0.25 * Math.pow(distance, 0.6);

        // Calculate dispersion factor
        double dispersionFactor = (emissionRate * Math.pow(stackHeight, 2)) / (2 * Math.PI * windSpeed * sigmaX * sigmaY * sigmaZ);

        return dispersionFactor;
    }
}

