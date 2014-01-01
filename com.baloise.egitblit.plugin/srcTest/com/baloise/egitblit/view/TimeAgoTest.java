package com.baloise.egitblit.view;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TimeAgoTest{

	private final static long SEC = 1000;
	private final static long MIN = SEC * 60;
	private final static long HOUR = MIN * 60;
	private final static long DAY = HOUR * 24;
	private final static long MONTH = DAY * 30;
	private final static long YEAR = MONTH * 12;
	
	Map<Long,TimeAgo> testMap = new HashMap<Long, TimeAgo>();
	
	@Before
	public void init(){
		this.testMap.put(0L,TimeAgo.Now);

		this.testMap.put(MIN-1,TimeAgo.Now);
		this.testMap.put(MIN,TimeAgo.Mins);
		this.testMap.put(MIN+1,TimeAgo.Mins);

		this.testMap.put(HOUR-1,TimeAgo.Mins);
		this.testMap.put(HOUR,TimeAgo.Hours);
		this.testMap.put(HOUR+1,TimeAgo.Hours);

		this.testMap.put(DAY-1,TimeAgo.Hours);
		this.testMap.put(DAY,TimeAgo.Days);
		this.testMap.put(DAY+1,TimeAgo.Days);
	
		this.testMap.put(MONTH-1,TimeAgo.Days);
		this.testMap.put(MONTH,TimeAgo.Months);
		this.testMap.put(MONTH+1,TimeAgo.Months);

		this.testMap.put(YEAR-1,TimeAgo.Months);
		this.testMap.put(YEAR,TimeAgo.Years);
		this.testMap.put(YEAR+1,TimeAgo.Years);
	}
	
	
	@Test
	public void testGetWhen(){
		for(Long time : this.testMap.keySet()){
			TimeAgo expected = this.testMap.get(time);
			TimeAgo is = TimeAgo.getAgo(time);
			assertTrue(expected.equals(is));
		}
	}
}
