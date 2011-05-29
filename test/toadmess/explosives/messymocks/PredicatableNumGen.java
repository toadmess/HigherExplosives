package toadmess.explosives.messymocks;

import java.util.Random;

@SuppressWarnings("serial")
public class PredicatableNumGen extends Random {
	private double counter = 0.0D;
	
	private final double increment;
	private final double modulo;
	
	public PredicatableNumGen(final double increment, final double modulo) {
		this.increment = increment;
		this.modulo = modulo;
	}
	
	public int getLastInt() { return (int) counter; }
	public long getLastLong() { return (long) counter; }
	
	public float getLastFloat() { return (float) counter; }
	public double getLastDouble() { return counter; }

	public int peekNextInt() { return (int) ((counter+increment)%modulo); }
	public long peekNextLong() { return (long) ((counter+increment)%modulo); }
	
	public float peekNextFloat() { return (float) ((counter+increment)%modulo); }
	public double peekNextDouble() { return (double) ((counter+increment)%modulo); }
	
	private void increment() {
		this.counter += this.increment;
		this.counter %= this.modulo;
	}
	
	@Override
	public double nextDouble() {
		increment();
		return counter; 
	}
	
	@Override
	public float nextFloat() {
		increment();
		return (float) counter;		
	}
	
	@Override
	public int nextInt() {
		increment();
		return (int) counter;		
	}
	
	@Override
	public long nextLong() {
		increment();
		return (long) counter;		
	}
}