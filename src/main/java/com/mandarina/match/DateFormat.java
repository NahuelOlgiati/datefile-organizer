package com.mandarina.match;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum DateFormat {
	LARGE("yyyyMMdd"), //
	SHORT("yyMMdd");

	private String format;

	DateFormat(String format) {
		this.format = format;
	}

	public static LocalDate get(Path path) {
		LocalDate r = null;
		outerloop: for (String n : getNumbers(path)) {
			for (DateFormat df : values()) {
				var localDate = getLocalDate(df, n);
				if (localDate != null) {
					r = localDate;
					break outerloop;
				}
			}
		}
		return r;
	}

	private static List<String> getNumbers(Path path) {
		var fn = path.getFileName().toString();
		var matcher = Pattern.compile("\\d+").matcher(fn);
		List<String> numbers = new ArrayList<>();
		while (matcher.find()) {
			numbers.add(matcher.group());
		}
		return numbers.stream().sorted(Comparator.comparingInt(String::length).reversed()).collect(Collectors.toList());
	}

	private static LocalDate getLocalDate(DateFormat df, String n) {
		LocalDate localDate = null;
		if (df.format.length() == n.length()) {
			try {
				localDate = LocalDate.parse(n, DateTimeFormatter.ofPattern(df.format));
			} catch (Exception e) {
			}
		}
		return localDate;
	}
}