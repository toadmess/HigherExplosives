package toadmess.explosives.stubbymocks;

import java.util.logging.Logger;

public class NullLogger extends Logger {
	public NullLogger() {
		super(null, null);
	}

	@Override
	public void config(String msg) {}

	@Override
	public void fine(String msg) {}

	@Override
	public void finer(String msg) {}

	@Override
	public void finest(String msg) {}

	@Override
	public void info(String msg) {}

	@Override
	public void warning(String msg) {}
}
