package com.codingame.game.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestUtils {
	
	private TestUtils() {}
	
	public static void setFieldPerReflection(Object instance, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		boolean accessible = field.isAccessible();
		field.setAccessible(true);
		field.set(instance, value);
		field.setAccessible(accessible);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getFieldPerReflection(Object instance, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		boolean accessible = field.isAccessible();
		field.setAccessible(true);
		Object value = field.get(instance);
		field.setAccessible(accessible);
		
		return (T) value;
	}
	
	public static void invokePrivateMethod(Object instance, String methodName, Object... parameters) throws Throwable {
		Class<?>[] parameterTypes = new Class<?>[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			parameterTypes[i] = parameters[i].getClass();
		}
		invokePrivateMethod(instance, methodName, parameterTypes, parameters);
	}
	
	public static void invokePrivateMethod(Object instance, String methodName, Class<?>[] parameterTypes, Object... parameters) throws Throwable {
		Method method = instance.getClass().getDeclaredMethod(methodName, parameterTypes);
		boolean accessible = method.isAccessible();
		method.setAccessible(true);
		try {
			method.invoke(instance, parameters);
		}
		catch (InvocationTargetException e) {
			throw e.getCause();
		}
		method.setAccessible(accessible);
	}
}
