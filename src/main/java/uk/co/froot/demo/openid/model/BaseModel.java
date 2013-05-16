package uk.co.froot.demo.openid.model;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.pegdown.PegDownProcessor;
import uk.co.froot.demo.openid.model.security.User;

import java.io.IOException;
import java.net.URL;

/**
 * <p>Base class to provide the following to views:</p>
 * <ul>
 * <li>Access to common data (user, adverts etc)</li>
 *
 * @since 0.0.1
 *         
 */
public class BaseModel {

  private User user;

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  /**
   * @return Some Markdown rendered as HTML - this is an inefficient way of performing this operation
   *         See the <code>/common/markdown.ftl</code> to see where it is displayed
   *
   * @throws IOException If something goes wrong
   */
  public String getMarkdownHtml() throws IOException {

    URL url = BaseModel.class.getResource("/views/markdown/demo-all-elements.md");
    String markdown = Resources.toString(url, Charsets.UTF_8).trim();

    // New processor each time due to pegdown not being thread-safe internally
    PegDownProcessor processor = new PegDownProcessor();

    // Return the rendered HTML
    return processor.markdownToHtml(markdown);

  }


}
