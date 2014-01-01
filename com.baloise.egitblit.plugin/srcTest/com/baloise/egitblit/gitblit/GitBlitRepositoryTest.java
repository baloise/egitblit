package com.baloise.egitblit.gitblit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GitBlitRepositoryTest{

	@Test
	public void testMakeByteValue(){
		
		long exp = 12345678901L;
		// Check formatting
		assertEquals(exp, GitBlitRepository.makeByteValue("123.4567.8901,23456"));
		assertEquals(exp, GitBlitRepository.makeByteValue("123,4567,8901.23456"));
		assertEquals(exp, GitBlitRepository.makeByteValue("123'4567'8901,23456"));
		assertEquals(exp, GitBlitRepository.makeByteValue("123'4567'8901.23456"));

		// Check computed values
		assertEquals(1L, GitBlitRepository.makeByteValue("1,00"));
		assertEquals(1L, GitBlitRepository.makeByteValue("1,00 b"));
		assertEquals(1024L, GitBlitRepository.makeByteValue("1,00 k"));
		assertEquals(1024L, GitBlitRepository.makeByteValue("1,00 kb"));
		assertEquals(1048576L, GitBlitRepository.makeByteValue("1,00 mb"));
		assertEquals(1073741824L, GitBlitRepository.makeByteValue("1,00 gb"));
		assertEquals(1099511627776L, GitBlitRepository.makeByteValue("1,00 tb"));

		assertEquals(1073741L, GitBlitRepository.makeByteValue("1,024 mb"));
		
	}

}
