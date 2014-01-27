package com.baloise.egitblit.gitblit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GitBlitRepositoryTest{

	@Test
	public void testMakeByteValue(){
		
		Long exp = new Long(12345678901L);
		// Check formatting
		assertEquals(exp, GitBlitRepository.makeByteValue("123.4567.8901,23456"));
		assertEquals(exp, GitBlitRepository.makeByteValue("123,4567,8901.23456"));
		assertEquals(exp, GitBlitRepository.makeByteValue("123'4567'8901,23456"));
		assertEquals(exp, GitBlitRepository.makeByteValue("123'4567'8901.23456"));

		// Check computed values
		assertEquals(new Long(1L), GitBlitRepository.makeByteValue("1,00"));
		assertEquals(new Long(1L), GitBlitRepository.makeByteValue("1,00 b"));
		assertEquals(new Long(1024L), GitBlitRepository.makeByteValue("1,00 k"));
		assertEquals(new Long(1024L), GitBlitRepository.makeByteValue("1,00 kb"));
		assertEquals(new Long(1048576L), GitBlitRepository.makeByteValue("1,00 mb"));
		assertEquals(new Long(1073741824L), GitBlitRepository.makeByteValue("1,00 gb"));
		assertEquals(new Long(1099511627776L), GitBlitRepository.makeByteValue("1,00 tb"));

		assertEquals(new Long(1073741L), GitBlitRepository.makeByteValue("1,024 mb"));
		
	}

}
