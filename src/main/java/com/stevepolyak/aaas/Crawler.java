package com.stevepolyak.aaas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Crawler {

	private final String NO_RESULTS = "there were no matching records for the specified sms id";
	Pattern gradesPattern = Pattern.compile("Grades<br />(.*?)<");
	Pattern misConPattern = Pattern
			.compile("<p><a href=\\\\\"http://assessment.aaas.org/misconceptions/(.*?)\\\\\" target=\\\\\"_blank\\\\\">(.*?)<\\\\/a><br />(.*?)<td>");
	Pattern percentagePattern = Pattern.compile("<p>.*?(\\d*)%.*?<\\\\/p>");

	public List<Misconception> crawl(String id, String parameterName, String url) {
		if (StringUtils.isEmpty(id) || StringUtils.isEmpty(parameterName)
				|| StringUtils.isEmpty(url)) {
			return null;
		}
		List<String> grades = new ArrayList<String>();
		List<Misconception> misconceptions = new ArrayList<Misconception>();

		try {

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(url + parameterName + "=" + id);
			getRequest.addHeader("accept", "application/json");

			HttpResponse response = httpClient.execute(getRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

			String results = getStringFromInputStream(response.getEntity()
					.getContent());
			if (results.toLowerCase().contains(NO_RESULTS)) {
				System.out.println("No content for: " + id);
				httpClient.getConnectionManager().shutdown();
				return null;
			} else {
				results = results.replace("<\\/tbody>", "<td><\\/tbody>");
				// fetch the grades
				Matcher matcher = gradesPattern.matcher(results);
				while (matcher.find()) {
					System.out.println(matcher.group(1));
					grades.add(matcher.group(1));
				}
				// fetch the misconceptions

				Matcher misconMatcher = misConPattern.matcher(results);
				while (misconMatcher.find()) {
					Misconception mis = new Misconception();

					mis.setId(misconMatcher.group(1));
					mis.setText(misconMatcher.group(2));
					String values = misconMatcher.group(3);
					Matcher percentMatcher = percentagePattern.matcher(values);
					while (percentMatcher.find()) {
						mis.getValues().add(percentMatcher.group(1));
					}
					misconceptions.add(mis);
				}

				for (Misconception m : misconceptions) {
					int count = 0;
					for (String grade : grades) {
						String temp = m.getValues().get(count);
						m.getValues().set(
								count,
								"Grade " + grade.replace("&ndash;", "-")
										+ " : " + temp + "%");
						count++;
					}
				}
			}

			httpClient.getConnectionManager().shutdown();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return misconceptions;
	}

	private static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
}