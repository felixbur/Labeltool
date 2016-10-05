package com.felix.util;

public class KeyValueInteger {
	private int _key = -1, _value = -1;

	public KeyValueInteger(int _key, int _value) {
		super();
		this._key = _key;
		this._value = _value;
	}

	public int get_key() {
		return _key;
	}

	public void set_key(int _key) {
		this._key = _key;
	}

	public int get_value() {
		return _value;
	}

	public void set_value(int _value) {
		this._value = _value;
	}

}
