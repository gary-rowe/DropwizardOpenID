package uk.co.froot.demo.openid.model;

import org.pegdown.PegDownProcessor;
import uk.co.froot.demo.openid.model.security.User;

import java.io.IOException;

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
   *         See the <code>/common/home.ftl</code> to see where it is displayed
   *
   * @throws IOException If something goes wrong
   */
  public String getHtml() throws IOException {

    // Hard coded but could come from anywhere
    String markdown = "### Example Links (built from Markdown)\n" +
      "\n" +
      "[Access protected info](/private/home): available to anyone after authentication.\n" +
      "\n" +
      "[Access private info](/private/admin): only available to people who authenticate with the specific email address set in `PublicOpenIDResource`.\n";

    // New processor each time due to pegdown not being thread-safe internally
    PegDownProcessor processor = new PegDownProcessor();

    // Return the rendered HTML
    return processor.markdownToHtml(markdown);

  }
}
