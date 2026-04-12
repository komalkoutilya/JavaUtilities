package commonutils.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Contain methods to transform and deal with html snippets.
 */
public class HTMLUtils {
    public static final Properties htmlProperties = new Properties();
    private static final String HTML_TAG_PREFIX = "html.tag";

    static {
        try {
            htmlProperties.load(HTMLUtils.class.getClassLoader().getResourceAsStream("html.properties"));
        } catch (IOException loadingException) {
            throw new RuntimeException("Failed to load html.properties", loadingException);
        }
    }

    private String htmlSnippet;

    public HTMLUtils(String htmlSnippet) {
        this.htmlSnippet = htmlSnippet;
    }

    public List<String> getAllHtmlTags() {
        return HTMLUtils.htmlProperties.entrySet().stream().filter(entry -> entry.getKey().toString().startsWith(HTMLUtils.HTML_TAG_PREFIX)).map(htmlTag -> htmlTag.getValue().toString()).collect(Collectors.toList());
    }

    public boolean isHtmlTag(String tagName) {
        return this.getAllHtmlTags().contains(tagName);
    }
}