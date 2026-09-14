package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.adapter.JsonbAdapter;
import java.util.Date;

//Class used to test JSONB Annotation
public class DateAdapter implements JsonbAdapter<Date, String> {
	public String adaptToJson(Date date) {
		return date.toString();
	}

	public Date adaptFromJson(String str) {
		return new Date(str);
	}
}