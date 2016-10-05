package com.felix.util.logging;


public interface LoggerInterface {
	public void error(String message);

	public void debug(String message);

	public void info(String message) ;

	public void warn(String message);

	public void fatal(String message);

	public void trace(String message);

	public void configure(String configPath);
	public boolean isDebugEnabled();

}
