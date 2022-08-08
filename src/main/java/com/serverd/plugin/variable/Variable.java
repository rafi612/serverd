package com.serverd.plugin.variable;

public class Variable<T> 
{
	private T value;

	public Variable<T> set(T value)
	{
		this.value = value;
		return this;
	}
	
	public T get()
	{
		return value;
	}
}
