package com.codingame.game.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TestUtils {
	
	private TestUtils() {}
	
	public static void setFieldPerReflection(Object instance, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		boolean accessible = field.canAccess(instance);
		field.setAccessible(true);
		field.set(instance, value);
		field.setAccessible(accessible);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getFieldPerReflection(Object instance, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		boolean accessible = field.canAccess(instance);
		field.setAccessible(true);
		Object value = field.get(instance);
		field.setAccessible(accessible);
		
		return (T) value;
	}
	
	public static void invokePrivateMethod(Object instance, String methodName, Object... parameters) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method = instance.getClass().getDeclaredMethod(methodName);
		boolean accessible = method.canAccess(instance);
		method.setAccessible(true);
		method.invoke(instance, parameters);
		method.setAccessible(accessible);
	}
	
	// see: https://stackoverflow.com/a/3301720/8178842
	public static void setStaticFinalFieldPerReflection(Class<?> clazz, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field field = clazz.getDeclaredField(fieldName);
		
		// ignore that the field may be private
		boolean isAccessible = field.canAccess(null);
		field.setAccessible(true);
		
		// ignore that the field may be final
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		boolean isFinal = (modifiersField.getModifiers() & Modifier.FINAL) != 0;
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		modifiersField.setAccessible(false);
		
		field.set(null, value);
		
		field.setAccessible(isAccessible);
		if (isFinal) {
			field.setInt(field, field.getModifiers() & Modifier.FINAL);
		}
	}
}
