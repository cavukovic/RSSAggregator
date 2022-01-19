import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML tree full of links of XML RSS (version 2.0) feeds
 * from a given file into a single, organized HTML file.
 *
 * @author Charlie Vukovic
 *
 */
public final class RSSAggregator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSAggregator() {
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        //loop through all children
        for (int i = 0; i < xml.numberOfChildren(); i++) {
            //if match is found return index
            if (xml.child(i).label().equals(tag)) {
                return i;
            }

        }
        //no match return -1
        return -1;
    }

    /**
     * Processes one news item and returns one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @return String of one full table row
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static String processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        String processed = "";
        String date = "";
        String source = "";
        String title = "";

        //organizing date, source and title into one string of html code

        date += '\t' + "<td>"
                + item.child(getChildElement(item, "pubDate")).child(0).label()
                + "</td>";

        if (getChildElement(item, "source") >= 0) {
            source += "\n" + '\t' + "<td>" + '\n' + '\t' + '\t' + "<a href=\""
                    + item.child(getChildElement(item, "source"))
                            .attributeValue("url")
                    + "\">"
                    + item.child(getChildElement(item, "source")).child(0)
                    + "</a>" + '\n' + '\t' + "</td>";
        } else {
            source += "\n" + '\t' + "<td>No source available</td>";
        }

        if (getChildElement(item, "title") >= 0) {
            if (item.child(getChildElement(item, "title"))
                    .numberOfChildren() == 0) {
                title += "\n" + '\t' + "<td>" + '\n' + '\t' + '\t' + "<a>"
                        + "No title" + "</a>" + '\n' + '\t' + "</td>";
            } else {
                title += "\n" + '\t' + "<td>" + '\n' + '\t' + '\t' + "<a>"
                        + item.child(getChildElement(item, "title")).child(0)
                                .label()
                        + "</a>" + '\n' + '\t' + "</td>";
            }

        }
        if (getChildElement(item, "link") >= 0) {
            title = "";
            if (item.child(getChildElement(item, "title"))
                    .numberOfChildren() == 0) {
                title += "\n" + '\t' + "<td>" + '\n' + '\t' + '\t'
                        + "<a href=\""
                        + item.child(getChildElement(item, "link")).child(0)
                                .label()
                        + "\">" + item.child(getChildElement(item, "link"))
                                .child(0).label()
                        + "</a>" + '\n' + '\t' + "</td>";
            } else {

                title += "\n" + '\t' + "<td>" + '\n' + '\t' + '\t'
                        + "<a href=\""
                        + item.child(getChildElement(item, "link")).child(0)
                                .label()
                        + "\">" + item.child(getChildElement(item, "title"))
                                .child(0).label()
                        + "</a>" + '\n' + '\t' + "</td>";
            }

        }
        //combine date source and time
        processed += "<tr>" + '\n' + date + source + title + "\n" + "</tr>";
        return processed;

    }

    /**
     * Converts RSS 2.0 feed to HTML and writes it to the file name given.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param file
     *            the name of the file being output to
     *
     * @ensures <pre>
     * file of name String file will have HTML code of XMLTree xml
     *
     * </pre>
     */
    private static void convertToHTML(XMLTree xml, String file) {
        SimpleWriter out = new SimpleWriter1L();
        SimpleWriter outFile = new SimpleWriter1L(file);

        //gets number of items that will go in table
        int numOfItems = 0;
        int count = 0;

        for (int i = 0; i < xml.child(0).numberOfChildren(); i++) {
            if (xml.child(0).child(i).label().equals("item")) {
                numOfItems++;

            }
        }
        int[] indexOfItems = new int[numOfItems];
        for (int f = 0; f < xml.child(0).numberOfChildren(); f++) {
            if (xml.child(0).child(f).label().equals("item")) {
                indexOfItems[count] = f;
                count++;
            }
        }
        String[] items = new String[numOfItems];
        for (int j = 0; j < indexOfItems.length; j++) {
            items[j] = processItem(xml.child(0).child(indexOfItems[j]), out);
        }

        //header
        String head = "";
        if (getChildElement(xml.child(0), "title") >= 0) {
            head += "<head>" + '\n' + '\t' + "<title>" + '\n' + '\t' + '\t'
                    + xml.child(0).child(getChildElement(xml.child(0), "title"))
                            .child(0).label()
                    + '\n' + '\t' + "</title>" + '\n' + "</head>";
        } else {
            head += "<head>" + '\n' + '\t' + "<title>" + '\n' + '\t' + '\t'
                    + "Empty Title" + '\n' + '\t' + "</title>" + '\n'
                    + "</head>";
        }

        //h1
        String h1 = "";

        if (getChildElement(xml.child(0), "link") >= 0) {
            h1 += "<h1>" + '\n' + '\t' + "<a href=\""
                    + xml.child(0).child(getChildElement(xml.child(0), "link"))
                            .child(0).label()
                    + "\">"
                    + xml.child(0).child(getChildElement(xml.child(0), "title"))
                            .child(0).label()
                    + "</a>" + '\n' + "</h1>";
        }

        //p1
        String p1 = "";
        if (xml.child(0).child(getChildElement(xml.child(0), "description"))
                .numberOfChildren() > 0) {
            p1 += "<p>" + '\n' + '\t' + "\""
                    + xml.child(0)
                            .child(getChildElement(xml.child(0), "description"))
                            .child(0).label()
                    + "\"" + '\n' + "</p>";
        } else {
            p1 += "<p>" + '\n' + '\t' + "\"No description\"" + '\n' + "</p>";
        }

        String HTML = "<html>" + '\n' + head + '\n' + "<body>" + '\n' + h1
                + '\n' + p1 + '\n' + "<table border = \"1\">" + '\n' + '\t'
                + "<tbody>" + '\n' + '\t';
        HTML += "<tr>" + '\n' + '\t' + "<th>Date</th>" + '\n' + '\t'
                + "<th>Source</th>" + '\n' + '\t' + "<th>News</th>" + '\n';
        for (int g = 0; g < items.length; g++) {
            HTML += items[g];
            HTML += '\n';
        }

        HTML += "</tbody>" + '\n' + '\t' + '\t' + "</table>" + '\n' + '\t'
                + "</body>" + '\n' + "</html>";
        outFile.print(HTML);
        outFile.close();
    }

    /**
     * Checks if the given XMLTree meets the requirements of an RSS 2.0 feed.
     *
     * @param xml
     *            the {@code XMLTree} to check
     *
     * @return whether or not given tree meets requirements of RSS 2.0 feed
     *
     *
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * isRSS =
     *  [boolean relating to whether or not tree is RSS 2.0 feed]
     * </pre>
     */
    private static boolean isRSS(XMLTree xml) {
        assert xml != null : "Violation of: xml is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";
        boolean channelChildren = false;
        boolean itemChildren = false;
        int itemIndex;
        int channelIndex = getChildElement(xml, "channel");
        if (channelIndex >= 0) {
            if (getChildElement(xml.child(channelIndex), "title") >= 0) {
                if (getChildElement(xml.child(channelIndex),
                        "description") >= 0) {
                    if (getChildElement(xml.child(channelIndex), "link") >= 0) {
                        if (xml.child(channelIndex).child(getChildElement(
                                xml.child(channelIndex), "link")) != null) {
                            channelChildren = true;
                        }
                    }
                }
            }
        }
        itemIndex = getChildElement(xml.child(channelIndex), "item");
        if (getChildElement(xml.child(channelIndex), "item") >= 0) {
            if (xml.child(channelIndex).child(
                    getChildElement(xml.child(channelIndex), "title")) != null
                    || xml.child(channelIndex).child(getChildElement(
                            xml.child(channelIndex), "description")) != null) {
                itemChildren = true;
                if (getChildElement(xml.child(channelIndex).child(itemIndex),
                        "source") >= 0) {
                    int sourceIndex = getChildElement(
                            xml.child(channelIndex).child(itemIndex), "source");
                    if (!xml.child(channelIndex).child(itemIndex)
                            .child(sourceIndex).hasAttribute("url")) {
                        itemChildren = false;
                    }
                }
            }

        }
        if (channelChildren && itemChildren) {
            return true;
        }
        return false;
    }

    /**
     * Processes one XML RSS (version 2.0) feed from a given URL converting it
     * into the corresponding HTML output file.
     *
     * @param url
     *            the URL of the RSS feed
     * @param file
     *            the name of the HTML output file
     * @param out
     *            the output stream to report progress or errors
     * @updates out.content
     * @requires out.is_open
     * @ensures <pre>
     * [reads RSS feed from url, saves HTML document with table of news items
     *   to file, appends to out.content any needed messages]
     * </pre>
     */
    private static void processFeed(String url, String file, SimpleWriter out) {

        XMLTree tree = new XMLTree1(url);
        if (isRSS(tree)) {
            convertToHTML(tree, file);
        }
    }

    /**
     * Processes one XML RSS (version 2.0) feed from a given URL converting it
     * into the corresponding HTML output file.
     *
     * @param tree
     *            the XMLTree of RSS 2.0 links
     * @param writeFile
     *            the name of the final HTML file that the home page will end up
     *            on
     *
     * @requires tree = valid XMLTree
     * @ensures <pre>
     * [reads XMLTree of links and then uses the processFeed method to make
     * an HTML document of news feeds]
     * </pre>
     */
    private static void processXMLFile(XMLTree tree, String writeFile) {
        SimpleWriter out = new SimpleWriter1L();
        int rssFeeds = 0;

        //making all opening brackets and such
        String HTML = "<html>" + "\n" + "<head>" + "\n" + "<title>"
                + tree.attributeValue("title") + "</title>" + "\n" + "<body>"
                + "\n" + "<h2>" + tree.attributeValue("title") + "</h2>" + "\n"
                + "<ul>" + "\n";

        for (int i = 0; i < tree.numberOfChildren(); i++) {
            if (tree.child(i).label().equals("feed")) {
                rssFeeds++;
            }
        }

        int[] indexOfFeeds = new int[rssFeeds];
        int count = 0;
        for (int f = 0; f < tree.numberOfChildren(); f++) {
            if (tree.child(f).label().equals("feed")) {
                indexOfFeeds[count] = f;
                count++;
            }
        }

        for (int j = 0; j < rssFeeds; j++) {
            String url = tree.child(indexOfFeeds[j]).attributeValue("url");
            String name = tree.child(indexOfFeeds[j]).attributeValue("name");
            String file = tree.child(indexOfFeeds[j]).attributeValue("file");
            processFeed(url, file, out);
            HTML += "<li>" + "\n" + "<a href=\"" + file + "\">" + name + "</a>"
                    + "\n" + "</li>" + "\n";
        }

        HTML += "</ul>" + "\n" + "</body>" + "\n" + "</html>";
        SimpleWriter outFile = new SimpleWriter1L(writeFile);
        outFile.print(HTML);
        outFile.close();
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        out.print("Name of the XML file containing RSS 2.0 feed(s): ");
        String xmlFile = in.nextLine();
        out.print(
                "Name of the HTML file that you would like your home page to be: ");
        String writeFile = in.nextLine();

        //new tree with url
        XMLTree tree = new XMLTree1(xmlFile);

        //process xml file, get links, titles, etc.
        processXMLFile(tree, writeFile);

        in.close();
        out.close();
    }

}
