package com.example.nhom3_tt_.util;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class VNPayUtilTest {

  @Test
  void hmacSHA512_matchesExpected() throws Exception {
    String key = "key";
    String data = "data";

    Mac mac = Mac.getInstance("HmacSHA512");
    SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
    mac.init(secretKey);
    byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    StringBuilder sb = new StringBuilder(2 * result.length);
    for (byte b : result) {
      sb.append(String.format("%02x", b & 0xff));
    }
    String expected = sb.toString();

    assertEquals(expected, VNPayUtil.hmacSHA512(key, data));
    assertEquals("", VNPayUtil.hmacSHA512(null, data));
    assertEquals("", VNPayUtil.hmacSHA512(key, null));
  }

  @Test
  void getIpAddress_fromHeader() {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    Mockito.when(req.getHeader("X-FORWARDED-FOR")).thenReturn("1.2.3.4");
    assertEquals("1.2.3.4", VNPayUtil.getIpAddress(req));
  }

  @Test
  void getIpAddress_fromRemoteAddr_whenHeaderNull() {
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    Mockito.when(req.getHeader("X-FORWARDED-FOR")).thenReturn(null);
    Mockito.when(req.getRemoteAddr()).thenReturn("5.6.7.8");
    assertEquals("5.6.7.8", VNPayUtil.getIpAddress(req));
  }

  @Test
  void getRandomNumber_lengthAndDigits() {
    String num = VNPayUtil.getRandomNumber(10);
    assertEquals(10, num.length());
    assertTrue(num.matches("\\d{10}"));
  }

  @Test
  void getPaymentURL_encodesAndOrders() {
    Map<String, String> map = new HashMap<>();
    map.put("b", "value b");
    map.put("a", "value a");
    map.put("c", "");
    map.put("d", null);

    String url = VNPayUtil.getPaymentURL(map, true);
    String[] parts = url.split("&");
    assertEquals(2, parts.length);
    assertEquals("a=" + URLEncoder.encode("value a", StandardCharsets.US_ASCII), parts[0]);
    assertEquals("b=" + URLEncoder.encode("value b", StandardCharsets.US_ASCII), parts[1]);
  }
}
