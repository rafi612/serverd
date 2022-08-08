package com.serverd.plugin.variable;

import java.util.HashMap;

public interface VariableStorage
{
	HashMap<String, Variable<?>> variables = new HashMap<>();
	
	default Variable<?> var(String name)
	{
		return variables.get(name);
	}
	
	default <V> Variable<V> newVariable(String name,Class<V> type)
	{
		Variable<V> var = new Variable<V>();
		variables.put(name, var);
		return var;
	}
	
	default void deleteVariable(String name)
	{
		variables.remove(name);
	}

}
