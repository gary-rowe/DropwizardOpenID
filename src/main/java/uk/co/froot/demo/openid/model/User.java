package uk.co.froot.demo.openid.model;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * <p>Simple representation of a User to provide the following to Resources:<br>
 * <ul>
 * <li>Storage of user state</li>
 * </ul>
 * </p>
 */
public class User {

  private String userName;
  private String password;
  private String firstName;
  private String lastName;
  private String emailAddress;
  private String openIDIdentifier;
  private Set<Authority> authorities=Sets.newHashSet();

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getOpenIDIdentifier() {
    return openIDIdentifier;
  }

  public void setOpenIDIdentifier(String openIDIdentifier) {
    this.openIDIdentifier = openIDIdentifier;
  }

  public void setAuthorities(Set<Authority> authorities) {
    this.authorities = authorities;
  }

  public boolean hasAllAuthorities(Set<Authority> requiredAuthorities) {
    return authorities.containsAll(requiredAuthorities);
  }

  public boolean hasAuthority(Authority authority) {
    return hasAllAuthorities(Sets.newHashSet(authority));
  }

}
