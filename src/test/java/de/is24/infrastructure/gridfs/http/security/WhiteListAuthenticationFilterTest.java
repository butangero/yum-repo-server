package de.is24.infrastructure.gridfs.http.security;

import de.is24.infrastructure.gridfs.http.utils.HostnameResolver;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;


public class WhiteListAuthenticationFilterTest {
  private static final String LOCAL_IP = "127.0.0.1";
  private static final String LOADBALANCER_IP = "10.99.10.12";
  private static final String X_FORWARDED_FOR = "X-Forwarded-For";
  private static final String ARBITRARY_IP = "192.168.5.5";
  private static final String ANOTHER_IP = "192.168.6.6";

  private HostnameResolver hostnameResolver;

  @Before
  public void setup() {
    this.hostnameResolver = new HostnameResolver(LOADBALANCER_IP);
  }

  @Test
  public void detectHostnameFromIP() throws Exception {
    WhiteListAuthenticationFilter filter = new WhiteListAuthenticationFilter(localHostname(), null, hostnameResolver);
    assertThat(filter.getPreAuthenticatedPrincipal(request(LOCAL_IP)), notNullValue());
  }

  @Test
  public void allowLoadBalancerRequestsWithXForwardedFor() throws Exception {
    WhiteListAuthenticationFilter filter = new WhiteListAuthenticationFilter(ARBITRARY_IP, null, hostnameResolver);
    assertThat(filter.getPreAuthenticatedCredentials(request(LOADBALANCER_IP, ARBITRARY_IP)), notNullValue());
  }

  @Test
  public void allowLoadBalancerRequestsWithXForwardedForChain() throws Exception {
    WhiteListAuthenticationFilter filter = new WhiteListAuthenticationFilter(ARBITRARY_IP, null, hostnameResolver);
    assertThat(filter.getPreAuthenticatedCredentials(request(LOADBALANCER_IP, ANOTHER_IP + ", " + ARBITRARY_IP)),
      notNullValue());
  }

  @Test
  public void allowLoadBalancerRequestsWithXForwardedForResolvableHostname() throws Exception {
    WhiteListAuthenticationFilter filter = new WhiteListAuthenticationFilter(localHostname(), null, hostnameResolver);
    assertThat(filter.getPreAuthenticatedCredentials(request(LOADBALANCER_IP, LOCAL_IP)), notNullValue());
  }

  @Test
  public void denyLoadBalancerRequestsWithUnauthorizedXForwardedFor() throws Exception {
    WhiteListAuthenticationFilter filter = new WhiteListAuthenticationFilter(ARBITRARY_IP, null, hostnameResolver);
    assertThat(filter.getPreAuthenticatedCredentials(request(LOADBALANCER_IP, ANOTHER_IP)), nullValue());
  }

  @Test
  public void denyLoadBalancerRequestsWithUnauthorizedXForwardedForChain() throws Exception {
    WhiteListAuthenticationFilter filter = new WhiteListAuthenticationFilter(ARBITRARY_IP, null, hostnameResolver);
    assertThat(filter.getPreAuthenticatedCredentials(request(LOADBALANCER_IP, ARBITRARY_IP + ", " + ANOTHER_IP)),
      nullValue());
  }

  @Test
  public void denyLoadBalancerRequestsWithoutXForwardedFor() throws Exception {
    WhiteListAuthenticationFilter filter = new WhiteListAuthenticationFilter("", null, hostnameResolver);
    assertThat(filter.getPreAuthenticatedCredentials(request(LOADBALANCER_IP)), nullValue());
  }

  private String localHostname() throws UnknownHostException {
    return Inet4Address.getByName(LOCAL_IP).getHostName();
  }

  private MockHttpServletRequest request(String ip) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteHost(ip);
    request.setRemoteAddr(ip);
    return request;
  }

  private HttpServletRequest request(String loadBalancerIP, String ip) {
    MockHttpServletRequest request = request(loadBalancerIP);
    request.addHeader(X_FORWARDED_FOR, ip);
    return request;
  }
}
