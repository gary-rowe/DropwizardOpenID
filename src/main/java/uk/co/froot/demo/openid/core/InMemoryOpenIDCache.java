package uk.co.froot.demo.openid.core;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.openid4java.consumer.ConsumerManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>In-memory cache to provide the following to OpenID authentication:</p>
 * <ul>
 * <li>Short term storage of thread local session data</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public enum InMemoryOpenIDCache {

  INSTANCE;

  /**
   * Simple cache for {@link org.openid4java.consumer.ConsumerManager} entries
   */
  private final Cache<UUID, ConsumerManager> consumerManagerCache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(2, TimeUnit.MINUTES)
    .build();

  /**
   * <h3>Note that this is not horizontally scalable</h3>
   *
   * @param sessionToken The session token
   *
   * @return The mapped OpenID {@link org.openid4java.consumer.ConsumerManager} created for that session token
   */
  public Optional<ConsumerManager> getConsumerManager(UUID sessionToken) {

    return Optional.fromNullable(consumerManagerCache.getIfPresent(sessionToken));

  }

  /**
   * <h3>Note that this is not horizontally scalable</h3>
   *
   * @param sessionToken    The session token
   * @param consumerManager The OpenID ConsumerManager for this UUID
   */
  public void putConsumerManager(UUID sessionToken, ConsumerManager consumerManager) {

    consumerManagerCache.put(sessionToken, consumerManager);

  }

}
