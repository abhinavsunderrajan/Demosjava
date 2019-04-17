package networkutils;

import java.util.Random;

/**
 * SRandom is a convenient class that provides various methods for generating
 * random numbers. SRandom is a singleton and should be the ONLY source of
 * random numbers in the simulation. This makes it possible to achieve
 * deterministic behaviour when needed (usually for debugging) by choosing a
 * particular seed.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class SRandom {
    private Random random;
    private static SRandom instance = null;

    public static SRandom instance(int seed) {
	if (instance == null)
	    instance = new SRandom(seed);
	return instance;
    }

    private SRandom(int seed) {
	random = new Random(0);
    }

    public void seed(long seed) {
	random.setSeed(seed);
    }

    public double nextDouble() {
	return random.nextDouble();
    }

    public int nextInt(int n) {
	return random.nextInt(n);
    }

    public boolean nextBoolean() {
	return random.nextBoolean();
    }

    public boolean nextBoolean(double p) {
	return random.nextDouble() < p;
    }

    public double nextGaussian(double mean, double sd) {
	return random.nextGaussian() * sd + mean;
    }
}
