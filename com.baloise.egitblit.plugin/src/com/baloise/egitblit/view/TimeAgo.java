package com.baloise.egitblit.view;



public enum TimeAgo{
	Now			  (0L),
	Mins		  (60000L),
	Hours		  (60000L*60L),
	Yesterday	(60000L*60L*24L),
	Days		  (60000L*60L*24L),
	Months		(60000L*60L*24L*30L),
	Years		  (60000L*60L*24L*365L);
	
	private long ticks;
	
	TimeAgo(long ticks){
		this.ticks = ticks;
	}
	
	public static TimeAgo getAgo(long ticks){
		
	  // do check value by value to keep order
		if(Years.getValue(ticks) > 0){
		  // More than one year
			return Years;
		}
		
		if(Months.getValue(ticks) > 0){
		  // More than a month
			return Months;
		}
		
		if(Days.getValue(ticks) > 0){
		  // more than a day
			if(Days.getValue(ticks) == 1){
			  // exactly one day
				return Yesterday;
			}
			return Days;
		}
		
		if(Hours.getValue(ticks) > 0){
		  // just hours
			return Hours;
		}
		
		if(Mins.getValue(ticks) > 0){
		  // just minutes
			return Mins;
		}
		return Now;
	}
	
	public long getValue(long value){
		if(this.ticks == 0){
			return 0;
		}
		return value / this.ticks;
	}
}