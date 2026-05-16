package commonutils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
     * Extracts the value of a specific attribute from a given HTML tag inside an HTML snippet.
     *
     * @param htmlTag     name of HTML element that contains the required attribute (eg: img, a)
     * @param attribute   name of the HTML element holding the required attribute, whose value needs to be extracted (eg: src, href)
     * @param htmlSnippet raw HTML string containing the target tag
     * @return the extracted attribute value as {@code String} if a match is found;
     * {@code null} if the tag or attribute does not exist in the snippet
     */
    public static String getAttributeValue(String htmlTag, String attribute, String htmlSnippet) {
        Pattern tagPattern = Pattern.compile(String.format("<\\s*%s\\b[^>]*?\\b%s\\s*=\\s*(['\"])(.*?)\\1", htmlTag, attribute));
        Matcher attributeMatcher = tagPattern.matcher(htmlSnippet);
        if (attributeMatcher.find()) {
            String attributeValue = attributeMatcher.group(2);
            logger.info("'{}' attribute value of tag '{}' is: '{}'", attribute, htmlTag, attributeValue);
            return attributeValue;
        }
        logger.error("'{}' attribute value not found for tag: '{}'", attribute, htmlTag);
        return null;
    }
}