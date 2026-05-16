package commonutils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
            innerHTMLContentValues.add(tagInnerContentMatcher.group(1));
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
}