import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.*;

class BookContentHandler extends DefaultHandler {
	
	private String currElem = "";
	private boolean insideBook = false;
	private ArrayList<Hashtable<String, String>> books =
			new ArrayList<Hashtable<String, String>>();
	 
	 public void startElement(String namespaceURI,
             String localName,
             String qName, 
             Attributes atts)
            		 throws SAXException {
		 currElem = qName;
		 if (qName.equals("book")) {
			 books.add(new Hashtable<String, String>());
			 insideBook = true;
		 }
	 }
	 
	 public void endElement(String uri, String localName,
				String qName) throws SAXException {
				currElem = "";
				if (qName.equals("book")) {
					books.get(books.size()-1).remove("book");
					insideBook = false;
				}
			}
	 
	 public void characters(char[] ch, int start, int length) throws SAXException {
		 if (insideBook)
			 books.get(books.size()-1).put(currElem, new String(ch,start,length));
	 }
	 
	 public ArrayList<Hashtable<String, String>> getData() {
		 return books;
	 }
}

public class BooksDB {
	private static final String schemaPath = "schema.xsd";
	private static Connection connection;
	enum XMLAlg { DOM, SAX };
	
	private static void abort(String message) {
		System.out.println(message);
		System.exit(1);
	}
	
	private static ArrayList<Hashtable<String, String>> lookDocument(Document xml) {
		ArrayList<Hashtable<String, String>> books =
				new ArrayList<Hashtable<String, String>>();
		NodeList bookElems = xml.getChildNodes().item(0).getChildNodes();
		int len = bookElems.getLength();
		for (int i = 0; i < len; i++) {
			Node book = bookElems.item(i);
			if (book.getNodeName().equals("book")) {
				Hashtable<String, String> bookTable = new Hashtable<String, String>();
				NodeList keys = book.getChildNodes();
				int keysLen = keys.getLength();
				for (int j = 0; j < keysLen; j++) {
					Node node = keys.item(j);
					if (!node.getNodeName().equals("#text"));
						bookTable.put(node.getNodeName(), node.getTextContent());
				}
				books.add(bookTable);
			}
		}
		return books;
	}
	
	private static boolean parseXML(File f, XMLAlg alg) {
		try {
			SchemaFactory schemaFactory =
				SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(new StreamSource(schemaPath));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(f));
			switch(alg) {
			case SAX:
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setNamespaceAware(true);
				SAXParser parser = factory.newSAXParser();
				FileReader fin = new FileReader(f);
				BookContentHandler booksHandler = new BookContentHandler();
				parser.parse(new InputSource(fin), booksHandler);
				saveToDB(booksHandler.getData());
				fin.close();
				break;
			case DOM:
				DocumentBuilderFactory docbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = docbFactory.newDocumentBuilder();
				Document xml = builder.parse(f);
				saveToDB(lookDocument(xml));
				break;
			}
		} catch (ParserConfigurationException | SAXException e) {
			System.out.println("Parsing error!\n"+e.getMessage());
			return false;
		}
		catch (FileNotFoundException e) {
			System.out.println("File "+f.getName()+" not found!");
			return false;
		}
		catch (IOException e) {
			System.out.println("I/O error!");
			return false;
		}
		catch(SQLException e) {
			System.out.println("DB error!\n"+e.getMessage());
		}
		return true;
	}
	
	public static void saveToDB(ArrayList<Hashtable<String, String>> books)
			throws SQLException
	{
		String insertPart1 = "insert into `books`\n";
		for (Hashtable<String, String> book : books) {
			String dataQuery = "select ";
			String isbn = book.get("isbn"),
					author = book.get("author"),
					title = book.get("title");
			dataQuery += ((isbn!=null) ? "isbn,author,title" : "author,title");
			dataQuery += " from `books` ";
			String wherePart = "where " + ((isbn!=null) ? ("isbn like \""+isbn+"\"") :
				("author like \""+author+"\" && title like \""+title+"\""));
			dataQuery += wherePart;
			ResultSet result = connection.createStatement().executeQuery(dataQuery);
			if (result.first())
				connection.createStatement().execute("delete from `books`\n"+wherePart);
			Enumeration<String> keys = book.keys();
			String insertPart2 = "(";
			String key = keys.nextElement(),
					val = book.get(key);
			insertPart2 += "`"+key+"`";
			if (!key.equals("price")) val = "\""+val+"\"";
			String vals = val;
			key = keys.nextElement();
			while (keys.hasMoreElements()) {
				val = book.get(key);
				insertPart2 += (",`"+key+"`");
				if (!key.equals("price")) val = "\""+val+"\"";
				vals += (","+val);
				key = keys.nextElement();
			}
			insertPart2 += (")\nvalues("+vals+")");
			connection.createStatement().execute(insertPart1+insertPart2);
		}
	}
	
	public static void main(String[] args) {
		String usage = "Usage: <program> DOM|SAX";
		XMLAlg alg = null;
		if (args.length==1) {
			switch(args[0]) {
			case "SAX":
				alg = XMLAlg.SAX;
				break;
			case "DOM":
				alg = XMLAlg.DOM;
				break;
			default:
				abort(usage);
			}
		}
		else abort(usage);
		
		String connectionStr = "jdbc:mysql://localhost/booksdb?user=root&password=123";
		String createTableStr = "create table if not exists `books` (\n"+
				"`id` INT NOT NULL AUTO_INCREMENT,\n"+
				"`author` CHAR(100),\n" +
				"`title` CHAR(100),\n"+
				"`genre` CHAR(100),\n"+
				"`isbn` CHAR(100),\n"+
				"`price` FLOAT,\n"+
				"`publish_date` DATE,\n"+
				"`description` TEXT,\n"+
				"PRIMARY KEY(`id`)\n)";
		ArrayList<String> xmlFilenames = new ArrayList<String>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			while (true) {
				System.out.println("\n"+new Date()+": parsing started.");
				connection = DriverManager.getConnection(connectionStr);
				connection.createStatement().execute(createTableStr);
				File[] files = (new File(".")).listFiles();
				int xmlsCount = 0;
				for (File file : files) {
					String filename = file.getName();
					if (filename.endsWith(".xml") &&
						!xmlFilenames.contains(filename) && 
							parseXML(file, alg)) {
						System.out.println("New file "+filename+" parsed.");
						xmlFilenames.add(filename);
						xmlsCount++;
					}
				}
				System.out.println(xmlsCount+" files are interpreted.");
				connection.close();
				Thread.sleep(30000);
			}
		} catch (Exception e) {
			abort(e.toString()+" "+e.getMessage());
		}
	}

}
