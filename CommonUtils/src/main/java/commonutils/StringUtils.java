package commonutils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final Logger logger = LogManager.getLogger(StringUtils.class);
    private static final Properties htmlProperties = new Properties();

    static {
        try {
            InputStream htmlPropertyReaderStream = StringUtils.class.getClassLoader().getResourceAsStream("html.properties");
            if (htmlPropertyReaderStream == null) {
                logger.error("Properties file not found: html.properties");
            }
            htmlProperties.load(htmlPropertyReaderStream);
        } catch (IOException ioe) {
            logger.error("Error occurred when reading html.properties", ioe);
        }
    }

    public static String getAttributeValue(String htmlTag, String attribute, String htmlSnippet) {
        Pattern tagPattern = Pattern.compile(String.format("<\\s*%s\\b[^>]*?\\b%s\\s*=\\s*(['\"])(.*?)\\1", htmlTag, attribute));
        Matcher attributeMatcher = tagPattern.matcher(htmlSnippet);
        if (attributeMatcher.find()) {
            logger.info("{} attribute value found for tag: {}", attribute, htmlTag);
            return attributeMatcher.group(2);
        }
        logger.error("{} attribute value not found for tag: {}", attribute, htmlTag);
        return null;
    }
}