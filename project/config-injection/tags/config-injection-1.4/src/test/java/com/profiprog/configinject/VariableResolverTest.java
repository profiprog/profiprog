package com.profiprog.configinject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;


public class VariableResolverTest {

	@Test
	public void testReplaceDolar() {
		VariableResolver tested = new VariableResolver(new MapVariableSource("var:val"));
		//"$$" is replaced by single '$'
		//"${$}" is also replaced by single '$'
		//"$var" is evaluated to "val"
		//"$" at end of line is ignored because there isn't valid variable name 
		assertEquals("$var $var val $", tested.resolveStringValue("${$}var $$var $var $"));
	}

	@Test
	public void testNestedReplacing() {
		VariableResolver tested = new VariableResolver(new MapVariableSource("a:b,b:1,b1:45,c:48"));
		assertEquals("b 45 48", tested.resolveStringValue("$a ${${a}${b}} ${${b1}:${c}}"));
	}

	@Test
	public void testFailingVariableSource() {
		final String MSG = "TEST_01";
		VariableResolver tested = new VariableResolver(new VariableSource() {
			@Override
			public String getRawValue(String variableName) throws NullPointerException {
				throw new RuntimeException(MSG);
			}
		});
		try { tested.resolveStringValue("$a"); fail(); } catch (RuntimeException e) { assertEquals(MSG, e.getMessage()); }
		try { tested.resolveStringValue("$a"); fail(); } catch (RuntimeException e) { assertEquals(MSG, e.getMessage()); }
	}

	@Test
	public void testNullValue() {
		VariableResolver tested = new VariableResolver();
		try { tested.resolveStringValue("${a}"); fail(); } catch (IllegalStateException e) { assertEquals("Missing property a", e.getMessage()); }
		try { tested.resolveStringValue("${a}"); fail(); } catch (IllegalStateException e) { assertEquals("Missing property a", e.getMessage()); }
	}
	
	@Test
	public void testCircularSubstituion() {
		VariableResolver tested = new VariableResolver(new MapVariableSource("a:a$b, b:b${c}b, c:$d, d:${e}d, e:$b"));
		try {
			tested.resolveStringValue("$a");
			fail();
		} catch (IllegalStateException e) {
			assertEquals("Circular substitution a <- b* <- c <- d <- e <- b", e.getMessage());
		}
	}
}
