package com.profiprog.configinject.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class MapParserTest {

	@Test
	public void testParseEmptyMap() {
		assertEquals("{}", MapParser.parseMap("").toString());
		assertEquals("{}", MapParser.parseMap(",,,,,").toString());
		assertEquals("{}", MapParser.parseMap(" , , ").toString());
	}

	@Test
	public void testParseKeys() {
		assertEquals("{a=null}", MapParser.parseMap("a").toString());
		assertEquals("{a=null, b=null, c=null, d=null}", MapParser.parseMap(" a,b , c ,d,a,").toString());
		assertEquals("{=null}", MapParser.parseMap(" '', , ").toString());
	}

	@Test
	public void testParseNormal() {
		assertEquals("{a=45}", MapParser.parseMap("a:45").toString());
		assertEquals("{a=0, b=1, c=8, d=9}", MapParser.parseMap(" a : 5,b:1 , c:8  ,d: 9,a: 0 ,").toString());
	}

	@Test
	public void testParseWhitespace() {
		assertEquals("{a= 45 }", MapParser.parseMap("a:' 45 '").toString());
		assertEquals("{a= 5, b =1, c=8, d=9,  a =0}", MapParser.parseMap(" a :' 5' ,'b ':1 , c:8  ,d: 9,' a ': 0 ,").toString());
	}

	@Test
	public void testParseWithEscaping() {
		assertEquals("{a=45,0}", MapParser.parseMap("a:45\\,0").toString());
		assertEquals("{a=45,0, b=45, 0=null, c=45,0, d=45,0}", MapParser.parseMap("a:'45,0',b:45,0,c:45\\,0,d:'45\\,0'").toString());
	}

	@Test
	public void testParseQuotes() {
		assertEquals("{a=45,0, b=45,0, c=45,0',, d=' \"}", MapParser.parseMap("a:'45,0',b:\"45,0\",c:\"45,0',\",d:' \"").toString());
	}
}
