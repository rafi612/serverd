package com.serverd.plugin.variable;

public class Variable<T> 
{
	private T value;

	@SuppressWarnings("unchecked")
	public Variable<T> set(Object value)
	{
		this.value = (T) value;
		return this;
	}
	
	public T get()
	{
		return value;
	}
}
