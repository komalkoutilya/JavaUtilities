package commonutils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contain methods to transform and deal with HTML snippets.
 * This class loads html.properties in static block, if failed to load, it then throws {@code RuntimeException}
 */
public class HTMLUtils {
    private static final Logger logger = LoggerFactory.getLogger(HTMLUtils.class);
    private static final Properties htmlProperties = new Properties();
    private static final String HTML_TAG_PREFIX = "html.tag";
    private static final String TAG_MATCHING_PATTERN = "<\\s*%1$s\\b[^>]*>(.*?)</\\s*%1$s\\s*>";
    private static final String ATTRIBUTE_MATCHING_PATTERN = "<\\s*%1$s\\b[^>]*?\\b%2$s(?:\\s*=\\s*(?:(['\"])(.*?)\\3|([^>\\s]+)))?";

    static {
        try {
            InputStream htmlPropertyReaderStream = HTMLUtils.class.getClassLoader().getResourceAsStream("html.properties");
            if (htmlPropertyReaderStream == null)
                throw new FileNotFoundException("Properties file not found: html.properties");
            htmlProperties.load(htmlPropertyReaderStream);
        } catch (IOException loadingFailedException) {
            throw new RuntimeException("Failed to load html.properties", loadingFailedException);
        }
    }

    private static final String anchorTag = Optional.ofNullable(htmlProperties.getProperty("html.tag.anchor")).orElse("a");
    /**
     * Fetches all HTML tags from html.properties file
     *
     * @return List of HTML tags
     */
    public List<String> getAllHtmlTags() {
        return htmlProperties.entrySet().stream().filter(entry -> entry.getKey().toString().startsWith(HTML_TAG_PREFIX)).map(htmlTag -> htmlTag.getValue().toString()).collect(Collectors.toList());
    }

    /**
     * @param tagName Name of HMTL tag (eg: iframe)
     * @return true if {@code tagName} is valid HTML tag, false otherwise
     */
    public boolean isHtmlTag(String tagName) {
        return this.getAllHtmlTags().contains(tagName);
    }

    /**
     * Extracts the inner HTML values/ content from specific HTML tag in an HTML snippet.
     *
     * @param htmlTag     name of HTML element whose inner HTML need to be extracted
     * @param htmlSnippet raw HTML string containing target tag
     * @return the list of extracted Inner HTM values/ content as {@code List<String>} if a match found, else an empty list is returned
     */
    public static List<String> getTagInnerContent(String htmlTag, String htmlSnippet) {
        Pattern tagInnerContentPattern = Pattern.compile(String.format(TAG_MATCHING_PATTERN, htmlTag), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher tagInnerContentMatcher = tagInnerContentPattern.matcher(htmlSnippet);
        List<String> innerHTMLContentValues = new ArrayList<>();
        while (tagInnerContentMatcher.find()) {
            innerHTMLContentValues.add(tagInnerContentMatcher.group(1).strip());
        }
        logger.info("list of tag content values - {}: {}", htmlTag, innerHTMLContentValues);
        return innerHTMLContentValues;
    }

    /**
     * Extracts the values of a specific attribute from all instances of given HTML tag in an HTML snippet.
     *
     * @param htmlTag     name of HTML element that contains the required attribute (eg: img, a)
     * @param attribute   name of the HTML element holding the required attribute, whose value needs to be extracted (eg: src, href)
     * @param htmlSnippet raw HTML string containing the target tag
     * @return the list of extracted attribute value as {@code List<String>} if a match is found, else an empty list is returned
     */
    public static List<String> getAttributeValue(String htmlTag, String attribute, String htmlSnippet) {
        Pattern attributePattern = Pattern.compile(String.format(ATTRIBUTE_MATCHING_PATTERN, htmlTag, attribute), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher attributeMatcher = attributePattern.matcher(htmlSnippet);
        List<String> attributeValues = new ArrayList<>();
        while (attributeMatcher.find()) {
            attributeValues.add(attributeMatcher.group(2));
        }
        logger.info("list of attribute values - {}: {}", attribute, attributeValues);
        return attributeValues;
    }

    /**
     * Returns the link uri value of the anchor tag matching the given link text.
     *
     * @param linkText    the text inside the anchor tag
     * @param htmlSnippet the HTML snippet containing anchor tags
     * @return the link value of the matching anchor tag
     * @throws IllegalArgumentException if the link text is missing or occurs multiple times
     */
    public static String getLinkValue(String linkText, String htmlSnippet) {
        List<String> retrievedLinkTexts = getTagInnerContent(anchorTag, htmlSnippet);
        if (Collections.frequency(retrievedLinkTexts, Optional.ofNullable(linkText).map(String::strip).orElseThrow(() -> new IllegalArgumentException("Link text should not be null"))) != 1) {
            throw new IllegalArgumentException("There are multiple links with link text: " + linkText);
        }
        return getAttributeValue(anchorTag, "href", htmlSnippet).get(retrievedLinkTexts.indexOf(linkText));
    }

    /**
     * Extracts all anchor ({@code <a>}) tag records from the given HTML snippet.
     * <p>
     * This method retrieves the link text of all anchor tags and the corresponding {@code href} attribute values and stores them in a {@link Map}, where:
     * <ul>
     *     <li>Key   = Link text</li>
     *     <li>Value = List of corresponding URI values</li>
     * </ul>
     * <p>
     * If multiple anchor tags contain the same link text, all associated href values are grouped into the same list.
     * <pre>
     * Example:
     * {@code
     * <a href="https://google.com">Google</a> <a href="https://gmail.com">Google</a>
     * }
     *
     * Output:
     * {
     *   "Google" -> [
     *       "https://google.com",
     *       "https://gmail.com"
     *   ]
     * }
     * </pre>
     *
     * @param htmlSnippet the HTML content from which anchor tag information should be extracted
     * @return a map containing link texts as keys and corresponding href attribute values as lists
     * @throws IllegalArgumentException if the provided HTML snippet is {@code null} or empty
     * @throws RuntimeException         if the number of extracted anchor texts and href attribute values do not match
     */
    public static Map<String, List<String>> getAllAnchors(String htmlSnippet) throws IllegalArgumentException, RuntimeException {
        htmlSnippet = Optional.ofNullable(htmlSnippet).map(String::strip).orElse("");
        if (htmlSnippet.isEmpty()) {
            throw new IllegalArgumentException("An empty HMTL snippet provided");
        }
        List<String> linkTexts = getTagInnerContent(anchorTag, htmlSnippet);
        logger.debug("Success - Retrieved link texts from html snippet");
        List<String> linkValues = getAttributeValue(anchorTag, "href", htmlSnippet);
        logger.debug("Success - Retrived URI values from html snippet");
        if (linkTexts.size() != linkValues.size())
            throw new RuntimeException("Error occurred: Link texts and Link values are in different count");
        Map<String, List<String>> anchorRecords = new HashMap<>();
        for (int iterator = 0; iterator < linkTexts.size(); iterator++) {
            anchorRecords.computeIfAbsent(linkTexts.get(iterator), lt -> new ArrayList<>()).add(linkValues.get(iterator));
        }
        return anchorRecords;
    }
}