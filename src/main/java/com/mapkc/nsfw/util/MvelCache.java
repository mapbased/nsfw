//package com.dianziq.nsfw.util;
//
//import java.io.Serializable;
//
//import org.mvel2.MVEL;
//
//import com.google.common.base.Function;
//
//public class MvelCache {
//
//	public static java.util.Map<String, Serializable> content = new com.google.common.collect.MapMaker()
//			.weakValues().makeComputingMap(
//					new Function<String, Serializable>() {
//
//						@Override
//						public Serializable apply(final String el) {
//							return MVEL.compileExpression(el);
//						}
//
//					});
//
//	@Deprecated
//	// :不利于优化
//	/**
//	 * mvel 两种优化方式，均需要传入的参数的class固定，如果不固定，优化之后出错
//	 * 解决的方法，为每个编译后表达式携带对应的class
//	 * @param exp
//	 * @return
//	 */
//	public static java.io.Serializable getCompiled(String exp) {
//		// TODO :check
//		return content.getTarget(exp);
//
//		// return MVEL.compileExpression(exp);
//	}
//
//	public static java.util.Map<Class, java.util.Map<String, Serializable>> cache = new com.google.common.collect.MapMaker()
//			.weakKeys().makeComputingMap(
//					new Function<Class, java.util.Map<String, Serializable>>() {
//
//						@Override
//						public java.util.Map<String, Serializable> apply(
//								final Class c) {
//							return new com.google.common.collect.MapMaker()
//									.weakKeys()
//									.makeComputingMap(
//											new Function<String, Serializable>() {
//
//												@Override
//												public Serializable apply(
//														final String el) {
//													return MVEL
//															.compileExpression(el
//																	.intern());
//												}
//
//											});
//						}
//
//					});
//
//	public static java.io.Serializable getCompiled(String exp, Class c) {
//		// TODO :check
//		// return content.getTarget(exp);
//
//		return cache.getTarget(c).getTarget(exp);
//		// return MVEL.compileExpression(exp);
//	}
//
// }
