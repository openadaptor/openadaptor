/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.auxil.connector.jndi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 * This class models a JNDI connection, as a bean.
 * <p>
 * It holds various JNDI connection parameters, and provides a connect() method to which will use the provided
 * parameters to generate a corresponding DirContext.
 * 
 * @see javax.naming.directory.DirContext for further information.
 * @author Eddy Higgins
 */
public class JNDIConnection {
  
  private static final Log log = LogFactory.getLog(JNDIConnection.class);

  /**
   * The default authentication is simple authentication (username/password)
   */
  public static final String DEFAULT_SECURITY_AUTHENTICATION = "simple";

  private String _initialContextFactory;

  private String _providerUrl;

  private String _alternateProviderUrl;

  private String _securityAuthentication;

  private String _securityPrincipal;

  private String _securityCredentials;

  // BEGIN Bean getters/setters

  /**
   * Assign the InitialContextFactory for this connection Object.
   * <p>
   * Example (LDAP): <blockquote>
   * 
   * <pre>
   * setInitialContextFactory(&quot;com.sun.jndi.ldap.LdapCtxFactory&quot;);
   * </pre>
   * 
   * </blockquote>
   * 
   * @param initialContextFactory
   *          The initialContextFactory to use.
   */
  public void setInitialContextFactory(String initialContextFactory) {
    _initialContextFactory = initialContextFactory;
  }

  /**
   * Return the InitialContextFactory for this connection Object.
   * 
   * @return The configured initialContextFactory
   */
  public String getInitialContextFactory() {
    return _initialContextFactory;
  }

  /**
   * Assign a provider Url for this connection.
   * <p>
   * Example: <blockquote>
   * 
   * <pre>
   * setProviderUrl(&quot;ldap://myldapserver.myCompany.com:389&quot;);
   * </pre>
   * 
   * </blockquote>
   * 
   * @param providerUrl
   */
  public void setProviderUrl(String providerUrl) {
    _providerUrl = providerUrl;
  }

  /**
   * Get the provider URL for this connection.
   * 
   * @return String containing the provider URL.
   */
  public String getProviderUrl() {
    return _providerUrl;
  }

  /**
   * Assign an alternate provider Url for this connection. This is used only
   * if there is a problem accessing the primary provider URL.
   * <p>
   * Example: <blockquote>
   *
   * <pre>
   * setAlternateProviderUrl(&quot;ldap://myldapserver.myCompany.com:389&quot;);
   * </pre>
   *
   * </blockquote>
   *
   * @param alternateProviderUrl
   */
  public void setAlternateProviderUrl(String alternateProviderUrl) {
    this._alternateProviderUrl = alternateProviderUrl;
  }

  /**
   * Get the alternate provider URL for this connection.
   *
   * @return String containing the alternate provider URL.
   */
  public String getAlternateProviderUrl() {
    return _alternateProviderUrl;
  }

  /**
   * Set the authentication type to use for this connection.
   * <p>
   * Example: <blockquote>
   * 
   * <pre>
   * setProviderUrl(&quot;ldap://myldapserver.myCompany.com:389&quot;);
   * </pre>
   * 
   * </blockquote>
   * 
   * @param securityAuthentication
   *          String which defines the type to use.
   * 
   */


  public void setSecurityAuthentication(String securityAuthentication) {
    _securityAuthentication = securityAuthentication;
  }

  /**
   * Assign a security principal for this connection.
   * <p>
   * Example: <blockquote>
   * 
   * <pre>
   * setSecurityPrincipal(&quot;uid=user1001,ou=ApplicationAccounts,ou=Resources,o=myCompany.com&quot;);
   * </pre>
   * 
   * </blockquote>
   * 
   * @param securityPrincipal
   *          String containing the security principle to use for the connection
   */
  public void setSecurityPrincipal(String securityPrincipal) {
    _securityPrincipal = securityPrincipal;
  }

  public String getSecurityPrincipal() {
    return _securityPrincipal;
  }

  /**
   * Set the security credentials for this connection.
   * <p>
   * Example: <blockquote>
   * 
   * <pre>
   * setSecurityCredentials(&quot;myTrickyPassword&quot;);
   * </pre>
   * 
   * </blockquote>
   * 
   * @param securityCredentials
   *          A String containing the credentials to use.
   */
  public void setSecurityCredentials(String securityCredentials) {
    _securityCredentials = securityCredentials;
  }

  /**
   * Get the security credentials for this connection
   * 
   * @return String containing the credentials it is using.
   */
  public String getSecurityCredentials() {
    return _securityCredentials;
  }

  /**
   * Default constructor for JNDIConnection.
   * <p>
   * By default, it sets the authentication to <tt>DEFAULT_SECURITY_AUTHENTICATION</tt>. this can be subsequently
   * changed using setSecurityAuthentication().
   */
  public JNDIConnection() { // No-arg constructor for beans
    setSecurityAuthentication(DEFAULT_SECURITY_AUTHENTICATION);
  }

  /**
   * Connect to a JNDI Service.
   * 
   * @return DirContext obtained.
   * @throws NamingException
   */
  public DirContext connect() throws NamingException {
    return new InitialDirContext(getConnectionProperties());
  }

  public Properties getConnectionProperties() {
    Properties env = new Properties();
    if (_initialContextFactory != null) {
      env.put(Context.INITIAL_CONTEXT_FACTORY, _initialContextFactory);
    }
    if (_providerUrl != null) {
      env.put(Context.PROVIDER_URL, _providerUrl);
    }
    // Authentication details
    if (_securityAuthentication != null) {
      env.put(Context.SECURITY_AUTHENTICATION, _securityAuthentication);
    }
    if (_securityPrincipal != null) {
      env.put(Context.SECURITY_PRINCIPAL, _securityPrincipal);
    }
    if (_securityCredentials != null) {
      env.put(Context.SECURITY_CREDENTIALS, _securityCredentials);
    }
    return env;
  }

  /**
   * Connect to a JNDI Service using the Alternate Provider URL.
   *
   * @return DirContext obtained.
   * @throws NamingException
   */
  public DirContext connectAlternate() throws NamingException {
    if ( _alternateProviderUrl != null ) {
      return new InitialDirContext(getAlternateConnectionProperties());
    }
    else {
      log.warn("Alternate Provider URL not Set. No alternative properties to try.");
      return null;
    }
  }

  public Properties getAlternateConnectionProperties() {
    Properties env = new Properties();
    if (_initialContextFactory != null) {
      env.put(Context.INITIAL_CONTEXT_FACTORY, _initialContextFactory);
    }
    if (_providerUrl != null) {
      env.put(Context.PROVIDER_URL, _alternateProviderUrl);
    }
    // Authentication details
    if (_securityAuthentication != null) {
      env.put(Context.SECURITY_AUTHENTICATION, _securityAuthentication);
    }
    if (_securityPrincipal != null) {
      env.put(Context.SECURITY_PRINCIPAL, _securityPrincipal);
    }
    if (_securityCredentials != null) {
      env.put(Context.SECURITY_CREDENTIALS, _securityCredentials);
    }
    return env;
  }
}
