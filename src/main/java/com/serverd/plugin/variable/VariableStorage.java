package com.serverd.plugin.variable;

import java.util.HashMap;

public class VariableStorage
{
	private HashMap<String, Variable<?>> variables = new HashMap<>();
	
	public Variable<?> var(String name)
	{
		return variables.get(name);
	}
	
	public <V> Variable<V> newVariable(String name,Class<V> type)
	{
		Variable<V> var = new Variable<V>();
		variables.put(name, var);
		return var;
	}
	
	public void deleteVariable(String name)
	{
		variables.remove(name);
	}

}
