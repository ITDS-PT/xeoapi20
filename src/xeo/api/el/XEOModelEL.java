package xeo.api.el;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 * A simple application that demonstrates the use of the
 * Unified Expression Language.
 * @author McDowell
 */
public class XEOModelEL {

	/**
	 * takes the javax.el.ExpressionFactory implementation class as an argument
	 * @param args
	 */
	public static void main(String[] args1) throws Exception {
		
		String impl = "com.sun.el.ExpressionFactoryImpl";
		
		//load the expression factory
		System.out.println("javax.el.ExpressionFactory="+impl);
		ClassLoader cl = XEOModelEL.class.getClassLoader();
		Class<?> expressionFactoryClass = cl.loadClass(impl);
		ExpressionFactory expressionFactory = (ExpressionFactory) expressionFactoryClass.newInstance();
		
		//create a map with some variables in it
		Map<Object, Object> userMap = new HashMap<Object, Object>();
		userMap.put("x", Integer.valueOf(123));
		userMap.put("y", Integer.valueOf(456));
		
		//get the method for ${myprefix:hello(string)}
		Method sayHello = XEOModelEL.class.getMethod("sayHello", new Class[]{String.class});
		
		//create the context
		ELResolver demoELResolver = new XEOELResolver(userMap);
		final VariableMapper variableMapper = new XEOVariableMapper();
		final XEOFunctionMapper functionMapper = new XEOFunctionMapper();
		functionMapper.addFunction("myprefix", "hello", sayHello);
		final CompositeELResolver compositeELResolver = new CompositeELResolver();
		compositeELResolver.add(demoELResolver);
		compositeELResolver.add(new ArrayELResolver());
		compositeELResolver.add(new ListELResolver());
		compositeELResolver.add(new BeanELResolver());
		compositeELResolver.add(new MapELResolver());
		ELContext context = new ELContext() {
			@Override
			public ELResolver getELResolver() {
				return compositeELResolver;
			}
			@Override
			public FunctionMapper getFunctionMapper() {
				return functionMapper;
			}
			@Override
			public VariableMapper getVariableMapper() {
				return variableMapper;
			}
		};
		
		//create and resolve a value expression
		String sumExpr = "${x+y}";
		ValueExpression ve = expressionFactory.createValueExpression(context, sumExpr, Object.class);
		Object result = ve.getValue(context);
		System.out.println("Result="+result);
		
		//call a function
		String fnExpr = "#{myprefix:hello('Dave')}";
		ValueExpression fn = expressionFactory.createValueExpression(context, fnExpr, Object.class);
		fn.getValue(context);
	}

	public static String sayHello(String argument) {
		System.out.println("Hello, "+argument);
		return "OK";
	}
	
}