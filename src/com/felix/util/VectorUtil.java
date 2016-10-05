package com.felix.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class VectorUtil {
	public static ArrayList<String> getUniqueValues(Collection<String> values)
	{
	    return new ArrayList<String>(new HashSet<String>(values));
	}
}
