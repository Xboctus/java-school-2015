import java.util.ArrayList;
import java.time.LocalDate;
import java.io.File;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class DomBookParser implements BookParser {
	@Override
	public ArrayList<Book> parse(String fileName) {
		ArrayList<Book> booksInFile = new ArrayList<Book>();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document d = db.parse(new File(fileName));
			NodeList books = d.getDocumentElement().getChildNodes();
			for (int i = 0; i < books.getLength(); ++i) {
				Node bookNode = books.item(i);
				if (!(bookNode instanceof Element)) {
					continue;
				}
				Element bookEl = (Element)bookNode;
				Book book = new Book();
				book.id = bookEl.getAttribute("id");
				NodeList props = bookEl.getChildNodes();
				for (int j = 0; j < props.getLength(); ++j) {
					Node propNode = props.item(j);
					if (!(propNode instanceof Element)) {
						continue;
					}
					Element propEl = (Element)propNode;
					String propValue = propEl.getTextContent();
					switch (propEl.getNodeName()) {
					case "author":
						book.author = propValue;
						break;
					case "title":
						book.title = propValue;
						break;
					case "genre":
						book.genre = propValue;
						break;
					case "price":
						book.price = Float.parseFloat(propValue);
						break;
					case "publish_date":
						book.publishDate = LocalDate.now(); // FIXME:
						break;
					case "description":
						book.description = propValue;
						break;
					case "isbn":
						book.isbn = propValue;
						break;
					}
				}
				booksInFile.add(book);
			}
			return booksInFile;
		} catch (Exception e) {
			return null;
		}
	}
}
