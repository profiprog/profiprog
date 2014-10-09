package com.profiprog.configinject;

import org.junit.Assert;
import org.junit.Test;

public class VariableParserTest {

	static class AssertionVariableParser extends VariableParser {

		StringBuilder sb = new StringBuilder();

		public AssertionVariableParser(String value) {
			super(value);
		}

		public AssertionVariableParser assertFind(String variableName, String defaultValue) {
			Assert.assertTrue(find());
			Assert.assertEquals(variableName, variableName());
			Assert.assertEquals(defaultValue, defaultValue());
			return this;
		}

		public AssertionVariableParser assertFind(String variableName) {
			Assert.assertTrue(find());
			Assert.assertEquals(variableName, variableName());
			Assert.assertNull(defaultValue());
			return this;
		}

		public AssertionVariableParser assertFindAndReplace(String variableName, String defaultValue, String replacement) {
			Assert.assertTrue(find());
			Assert.assertEquals(variableName, variableName());
			Assert.assertEquals(defaultValue, defaultValue());
			appendReplacement(sb, replacement);
			return this;
		}

		public AssertionVariableParser assertFindAndReplace(String variableName, String replacement) {
			Assert.assertTrue(find());
			Assert.assertEquals(variableName, variableName());
			Assert.assertNull(defaultValue());
			appendReplacement(sb, replacement);
			return this;
		}

		public AssertionVariableParser assertDefinition(String def) {
			Assert.assertEquals(def, definition());
			return this;
		}

		public AssertionVariableParser assertNothingElseFound() {
			Assert.assertFalse(find());
			return this;
		}

		public AssertionVariableParser assertTailResult(String value) {
			appendTail(sb);
			Assert.assertEquals(value, sb.toString());
			return this;
		}
	}

	@Test
	public void testSimpleFind() throws Exception {
		new AssertionVariableParser("${val}")
				.assertFind("val")
				.assertDefinition("${val}")
				.assertNothingElseFound();
		new AssertionVariableParser("$val")
				.assertFind("val")
				.assertDefinition("$val")
				.assertNothingElseFound();
		new AssertionVariableParser("${val:xyz}")
				.assertFind("val", "xyz")
				.assertDefinition("${val:xyz}")
				.assertNothingElseFound();

		new AssertionVariableParser("a ${a} b $b c ${c:d} $")
				.assertFind("a").assertDefinition("${a}")
				.assertFind("b").assertDefinition("$b")
				.assertFind("c", "d").assertDefinition("${c:d}")
				.assertNothingElseFound()
				.assertNothingElseFound();
	}

	@Test
	public void testFindWithNestedVariables() throws Exception {
		new AssertionVariableParser("a ${${x}} b $$ c ${i${y}:j${z}} $")
				.assertFind("${x}").assertDefinition("${${x}}")
				.assertFind("$").assertDefinition("$$")
				.assertFind("i${y}", "j${z}").assertDefinition("${i${y}:j${z}}")
				.assertNothingElseFound();
	}

	@Test
	public void testFindWithManyNestedVariables() throws Exception {
		new AssertionVariableParser("${1${${${3}}2}} - ${${${${${a}}}}:${${${${b}}}}}")
				.assertFind("1${${${3}}2}").assertDefinition("${1${${${3}}2}}")
				.assertFind("${${${${a}}}}", "${${${${b}}}}").assertDefinition("${${${${${a}}}}:${${${${b}}}}}")
				.assertNothingElseFound();
	}

	@Test
	public void testEscaping() throws Exception {
		new AssertionVariableParser("${$$} - ${$} $$$$ $")
				.assertFind("$$").assertDefinition("${$$}")
				.assertFind("$").assertDefinition("${$}")
				.assertFind("$").assertDefinition("$$")
				.assertFind("$").assertDefinition("$$")
				.assertNothingElseFound();
	}

	@Test
	public void testReplacing() throws Exception {
		new AssertionVariableParser("a ${${x}} b $$ c ${i${y}:j${z}} $")
				.assertFindAndReplace("${x}", "X").assertDefinition("${${x}}")
				.assertFindAndReplace("$", "$").assertDefinition("$$")
				.assertFindAndReplace("i${y}", "j${z}", "YZ").assertDefinition("${i${y}:j${z}}")
				.assertNothingElseFound()
				.assertTailResult("a X b $ c YZ $");
	}


}
