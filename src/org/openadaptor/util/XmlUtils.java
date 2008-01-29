/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.DOMParser;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Common XML Utilities for OA3
 * 
 * @author Eddy Higgins
 */
public class XmlUtils {

  private static final Log log = LogFactory.getLog(XmlUtils.class);

  private XmlUtils() {
  } // No instantiation allowed.

  /**
   * This method will extract all of the comment nodes from an XML Document. The comments are returned as a Comment[]
   * (in the order that they were encountered in the Document.
   * 
   * @param document
   *          DOM Document
   * 
   * @return Comment[] of the comments from the document.
   */
  public static Comment[] extractComments(Document document) {
    log.debug("Extracting comments from XML document");
    ArrayList comments = getComments(document);
    return (Comment[]) comments.toArray(new Comment[comments.size()]);
  }

  /**
   * This method will extract the comments from an XML file. All of the real work is done by extractComments(Document
   * document).
   * 
   * @param xmlFile
   *          XML File to use
   * @param validate
   *          If true, validate external DTD definitions.
   * 
   * @return Comment[] of comments from the document.
   * 
   * @throws SAXException
   *           if there's a problem with parsing
   * @throws IOException
   *           if there's a problem reading the file.
   */
  public static Comment[] extractComments(String xmlFile, boolean validate) throws SAXException, IOException {
    DOMParser parser = new DOMParser();
    // Parse document
    log.debug("Parsing XML file " + xmlFile);
    // parser.setFeature("http://xml.org/sax/features/validation",false);
    parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", validate);
    parser.parse(xmlFile);
    return extractComments(parser.getDocument());
  }

  /**
   * This method will extract the comments from an XML file without validating external DTDs. It is exactly equivalent
   * to extractComments(xmlFile,false);
   * 
   * @param xmlFile
   *          XML File to use
   * 
   * @return Comment[] of comments from the document.
   * 
   * @throws SAXException
   *           if there's a problem with parsing
   * @throws IOException
   *           if there's a problem reading the file.
   */

  public static Comment[] extractComments(String xmlFile) throws SAXException, IOException {
    return extractComments(xmlFile, false);
  }

  /**
   * Return a List of the comments contained by a node.
   * 
   * @param node
   * 
   * @return ArrayList of Comment objects.
   */
  private static ArrayList getComments(Node node) {
    ArrayList commentList = new ArrayList();
    getComments(node, commentList);
    return commentList;
  }

  /**
   * Recursively find the comments within a node, and add them to a supplied (not null) List)
   * 
   * @param node
   *          Node in which to search for comments
   * @param commentList
   *          List of comments which it will add to.
   */
  private static void getComments(Node node, List commentList) {
    // if (commentList==null) throw new IllegalArgumentException ("list may not be null");
    if (node != null) {
      if (Node.COMMENT_NODE == node.getNodeType()) { // Comment - add it
        commentList.add(node);
      } else {
        // Ignore
      }
      // Now recursively traverse the tree under this node.
      NodeList children = node.getChildNodes();
      if (children != null) { // Now need to do each child in turn.
        for (int i = 0; i < children.getLength(); i++) {
          getComments(children.item(i), commentList);
        }
      }
    }
  }

  /**
   * Uses a Dom4j SAXReader to apply formatting to the supplied XML fragment. In this case we use a prettyprinter to
   * indent the tags.
   * 
   * @param xml
   *          The XML to be formatted (can be just a fragment)
   * @param isFragment
   *          If you supplied a XML fragment then this must be set to true so that the writer doesn't output an XML
   *          declaration
   * 
   * @return the formatted XML
   * 
   * @throws IOException
   *           if there was a problem applying the formatting
   * @throws DocumentException
   *           if there was a problem with the XML
   */
  public static String format(String xml, boolean isFragment) throws IOException, DocumentException {

    OutputFormat format = OutputFormat.createPrettyPrint();
    format.setSuppressDeclaration(isFragment);
    format.setIndent("\t");

    SAXReader reader = new SAXReader();
    Reader r = new StringReader(xml);
    org.dom4j.Document document = reader.read(r);

    Writer w = new StringWriter();
    XMLWriter out = new XMLWriter(w, format);
    out.write(document);
    out.close();

    return w.toString();
  }

  /**
   * Simple sanity test method.
   * 
   * @param argv
   *          List of XML files to examine for comments.
   */
  public static void main(String[] argv) {
    for (int i = 0; i < argv.length; i++) {
      try {
        String xmlFile = argv[i];
        Comment[] comments = extractComments(xmlFile, false);
        log.info("------");
        // Read the entire document into memory
        for (int j = 0; j < comments.length; j++) {
          log.info("------");
          log.info(comments[j].getNodeValue()); // Xerces doesn't like getTextContent()
        }
        log.info("------");
        // work with the document...
      } catch (SAXException e) {
        log.error(e);
      } catch (IOException e) {
        log.error(e);
      }
    }
  }

  /**
   * Searches through the supplied Document for tags matching the supplied tagName with an attribute with the name/value
   * pair supplied
   * 
   * @param doc
   *          the document to search
   * @param tagName
   *          the tag name to search for
   * @param attributeName
   *          the attribute name to search the tag for
   * @param attributeValue
   *          the attribute value to search the tag for
   * 
   * @return a list of the matches
   */
  public static ArrayList getElementByTagName(Document doc, String tagName, String attributeName, String attributeValue) {
    log.debug("Searching for tags named [" + tagName + "] and an atttribute [" + attributeName + ", " + attributeValue
        + "]");

    ArrayList list = new ArrayList();

    if (doc == null || tagName == null || tagName.equals("") || attributeName == null || attributeName.equals(""))
      return list;

    if (attributeValue == null)
      attributeValue = "";

    // get list of potential tags
    NodeList nodes = doc.getElementsByTagName(tagName);
    log.debug(nodes.getLength() + " tag(s) found");

    // check each one for the named attribute and value
    for (int i = 0; i < nodes.getLength(); i++) {
      Node n = nodes.item(i);
      NamedNodeMap attrs = n.getAttributes();
      log.debug("Tag " + i + ": " + attrs.getLength() + " attribute(s) found");

      for (int j = 0; j < attrs.getLength(); j++) {
        Node a = attrs.item(j);

        if (a.getNodeName().equalsIgnoreCase(attributeName) && a.getNodeValue().equalsIgnoreCase(attributeValue)) {
          log.debug("Matching attribute found");
          list.add(n);
        }
      }
    }

    log.debug(list.size() + " tag(s) found");
    return list;
  }

  /**
   * Searches below the supplied Node for a "list" tag and then retrieves the contents of the "value" tag(s) under that.
   * Note that if there is more than one "list" under the node then we will take the values from the first one only.
   * 
   * @param node
   *          acts as the root for the seach
   * 
   * @return list of Strings corresponding to the contents of the "value" tag(s)
   */
  public static ArrayList getElementListValues(Node node) {
    ArrayList values = new ArrayList();

    // search for list tag
    Document doc = node.getOwnerDocument();
    NodeList list = doc.getElementsByTagName("list");

    if (list.getLength() == 0)
      return values;

    // search under that for value tag(s)
    doc = list.item(0).getOwnerDocument();
    NodeList vals = doc.getElementsByTagName("value");

    // for each one we get the text contents
    for (int i = 0; i < vals.getLength(); i++) {
      Node v = vals.item(i);
      NodeList text = v.getChildNodes();

      if (text == null) {
        values.add("");
        continue;
      }

      // there should be only text inside the value tag
      Node value = text.item(0);
      if (value == null)
        values.add("");
      else
        values.add(value.getNodeValue());
    }

    return values;
  }
}
