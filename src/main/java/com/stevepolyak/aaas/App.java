package com.stevepolyak.aaas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class App {

	private static final String AAAS_MISCONCEPTION_URL = "http://strandmaps.nsdl.org/cms1-2/jsapi/api_v1/bubble/aaasMisconception.jsp?";
	private static final String BM_ID = "bmId";

	public static void main(String argv[]) {

		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			File file = new File("/Users/polyaks/out.txt");
			FileOutputStream fop = new FileOutputStream(file);
			fop = new FileOutputStream(file);
			final OutputStreamWriter out = new OutputStreamWriter(fop, "UTF8");

			DefaultHandler handler = new DefaultHandler() {

				boolean identifier = true;
				List<Misconception> misconceptions = null;

				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					emit("<" + qName + ">");
					if (qName.equalsIgnoreCase("identifier")) {
						identifier = true;
					}
				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					if (qName.equalsIgnoreCase("oai_dc:dc")) {
						writeMiscons();
					}
					emit("</" + qName + ">");
				}

				public void characters(char ch[], int start, int length)
						throws SAXException {
					emit(new String(ch, start, length));
					Crawler crawler = new Crawler();

					if (identifier) {
						String id = new String(ch, start, length);
						if (id != null) {
							id = id.trim();
						}
						if (!StringUtils.isEmpty(id)) {
							misconceptions = crawler.crawl(id, BM_ID,
									AAAS_MISCONCEPTION_URL);
						}
						identifier = false;
					}
				}

				void writeMiscons() throws SAXException {
					if (misconceptions == null) {
						return;
					}
					if (misconceptions.size() < 1) {
						return;
					}

					for (Misconception m : misconceptions) {
						emit("<dc:miscon>");
						emit(m.getText() + "(id: " + m.getId() + ")(");
						for (String value : m.getValues()) {
							emit(value + " ");
						}
						emit(")</dc:miscon>");
					}
					misconceptions = null;
				}

				void emit(String s) throws SAXException {
					try {
						out.write(s);
						out.flush();
					} catch (IOException e) {
						throw new SAXException("I/O error", e);
					}
				}
			};

			saxParser.parse(
					ClassLoader.class.getResourceAsStream("/OAI-PMH.xml"),
					handler);
			fop.flush();
			fop.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}