package com.mattvorst.shared.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class TimeZoneUtils {
	public static final String DEFAULT_TIMEZONE = "US/Eastern";

	public static TimeZone standardTimeZone(String timeZoneString){
		TimeZone timeZone = null;

		if(Utils.empty(timeZoneString)){
			timeZone = null;
		}else{
			switch (timeZoneString) {
			case "America/New_York":
				timeZone = TimeZone.getTimeZone("US/Eastern");
				break;
			case "America/Chicago":
				timeZone = TimeZone.getTimeZone("US/Central");
				break;
			case "America/Denver":
				timeZone = TimeZone.getTimeZone("US/Mountain");
				break;
			case "America/Los_Angeles":
				timeZone = TimeZone.getTimeZone("US/Pacific");
				break;
			case "America/Anchorage":
				timeZone = TimeZone.getTimeZone("US/Alaska");
				break;
			case "Pacific/Honolulu":
				timeZone = TimeZone.getTimeZone("US/Hawaii");
				break;

			default:
				timeZone = TimeZone.getTimeZone(timeZoneString);

				if(!getTimeZones().contains(timeZone)){
					timeZone = null;
				}
				break;
			}
		}

		return timeZone;
	}

	public static List<TimeZone> getTimeZones(){

		List<TimeZone> list = new ArrayList<>();

		list.add(TimeZone.getTimeZone("GMT"));
		list.add(TimeZone.getTimeZone("US/Eastern"));
		list.add(TimeZone.getTimeZone("US/Central"));
		list.add(TimeZone.getTimeZone("US/Mountain"));
		list.add(TimeZone.getTimeZone("US/Pacific"));
		list.add(TimeZone.getTimeZone("US/Alaska"));
		list.add(TimeZone.getTimeZone("US/Hawaii"));

		return list;
	}
}
