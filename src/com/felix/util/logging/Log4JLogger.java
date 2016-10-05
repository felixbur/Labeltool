package com.felix.util.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class Log4JLogger implements LoggerInterface {
	Logger _logger;

	public Log4JLogger(Class c) {
		_logger = Logger.getLogger(c.getName());
	}
	public Log4JLogger(String name) {
		_logger = Logger.getLogger(name);
	}
	public Log4JLogger(String name,String pathToConfig) {
		DOMConfigurator.configure(pathToConfig);
		_logger = Logger.getLogger(name);
	}

	public Log4JLogger(Logger logger) {
		_logger = logger;
	}

	public void error(String message) {
		_logger.error(message);
	}

	public void debug(String message) {
		_logger.debug(message);
	}

	public void info(String message) {
		_logger.info(message);
	}

	public void warn(String message) {
		_logger.warn(message);
	}

	public void fatal(String message) {
		_logger.fatal(message);
	}

	public void trace(String message) {
		_logger.trace(message);
	}

	public void configure(String configPath) {
		DOMConfigurator.configure(configPath);
	}

	public boolean isDebugEnabled() {
		return _logger.isDebugEnabled();
	}
}
