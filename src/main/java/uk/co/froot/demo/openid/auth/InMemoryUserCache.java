package uk.co.froot.demo.openid.auth;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import uk.co.froot.demo.openid.model.security.User;

import java.util.concurrent.TimeUnit;

/**
 * <p>Cache to provide the following to {@link User} authenticators:</p>
 * <ul>
 * <li>In-memory thread-safe cache for client user instances</li>
 * <li>Provision of fast lookup for request authentication</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public enum InMemoryUserCache {

  // Provide a global singleton for the application
  INSTANCE;

  // A lot of threads will hit this cache
  private volatile Cache<String, User> userCache;

  InMemoryUserCache() {
    reset(15, TimeUnit.MINUTES);
  }

  /**
   * Resets the cache and allows the expiry time to be set (perhaps for testing)
   *
   * @param duration The duration before a user must manually authenticate through a web form due to inactivity
   * @param unit     The {@link java.util.concurrent.TimeUnit} that duration is expressed in
   */
  public InMemoryUserCache reset(int duration, TimeUnit unit) {

    // Build the cache
    if (userCache != null) {
      userCache.invalidateAll();
    }

    // If there is no activity against a key then we want
    // it to be expired from the cache, but each fresh write
    // will reset the expiry timer
    userCache = CacheBuilder
      .newBuilder()
      .expireAfterWrite(duration, unit)
      .maximumSize(1000)
      .build();

    return INSTANCE;
  }

  /**
   *
   * @param sessionId The session token to locate the user
   * @return The matching ClientUser or absent
   */
  public Optional<User> getBySessionId(String sessionId) {

    // Check the cache
    Optional<User> userOptional = Optional.fromNullable(userCache.getIfPresent(sessionId));

    if (userOptional.isPresent()) {
      // Ensure we refresh the cache on a check to maintain the session timeout
      userCache.put(sessionId, userOptional.get());
    }

    return userOptional;

  }

  /**
   * @param sessionId The OpenID token to use to locate the user
   * @param user        The client user to cache
   */
  public void put(String sessionId, User user) {
    Preconditions.checkNotNull(user);
    userCache.put(sessionId, user);
  }

}
