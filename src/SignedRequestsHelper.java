import java.io.UnsupportedEncodingException;

// import java.net.URLDecoder;
import java.net.URLEncoder;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
// import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Properties; 

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// import org.apache.commons.codec.binary.Base64;
import java.util.Base64;

// Preparing the REST Request for the Amazon Product Advertising API
// This is basically the sample code from http://docs.aws.amazon.com/AWSECommerceService/latest/DG/AuthJavaSampleSig2.html
// with four changes:
// 1) use Java 8 java.util.Base64 native class instead of another dependency
// 2) use java.util.Properties to configure the endpoint and "hide" my Amazon AWS keys
// 3) return only the signed uri parameters
// 4) solved the signature problem

public class SignedRequestsHelper {
  private static final String UTF8_CHARSET = "UTF-8";
  private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
  private static final String REQUEST_URI = "/onca/xml";
  private static final String REQUEST_METHOD = "GET";

// SWOBI: get the endpoint and keys from the properties file
  Properties p = AmazonAPI.properties;
  private String endpoint = p.getProperty("amazon.endpoint");
  private String awsAccessKeyId = p.getProperty("amazon.awsAccessKeyId");
  private String awsSecretKey = p.getProperty("amazon.awsSecretKey");

  private SecretKeySpec secretKeySpec = null;
  private Mac mac = null;
  
  public SignedRequestsHelper() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException 
  {
    byte[] secretyKeyBytes;
   
    secretyKeyBytes = awsSecretKey.getBytes(UTF8_CHARSET);
    secretKeySpec = new SecretKeySpec(secretyKeyBytes, HMAC_SHA256_ALGORITHM);
    mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
    mac.init(secretKeySpec);
  }

  public String sign(Map<String, String> params) 
  {
    params.put("AWSAccessKeyId", awsAccessKeyId);
    params.put("Timestamp", timestamp());

    SortedMap<String, String> sortedParamMap = new TreeMap<String, String>(params);
    String canonicalQS = canonicalize(sortedParamMap);
    String toSign =
      REQUEST_METHOD + "\n"
      + endpoint + "\n"
      + REQUEST_URI + "\n"
      + canonicalQS;

    String hmac = hmac(toSign);
//    String sig = percentEncodeRfc3986(hmac);

// Build signedParams for Camel HTTP4 component
// SWOBI: Since there is a double urlencoding/decoding problem in this component I had to use RAW and *no* percent encoding
// Otherwise a '+' in the base64 encoded signature would break the request (HTTP Error 403 - SignatureDoesNotMatch)
// see http://camel.apache.org/how-do-i-configure-endpoints.html    

// SWOBI: Code to build the whole HTTP4 URL here
//   String url = "http4://" + endpoint + REQUEST_URI + "?" + canonicalQS + "&Signature=RAW(" + hmac + ")";
//   return url;   
 
    String signedParams = canonicalQS + "&Signature=RAW(" + hmac + ")";

// SWOBI: Code to generate the sporadic SignatureDoesNotMatch error (can be used to test exception handling ;-)
//    String signedParams = canonicalQS + "&Signature=" + hmac;
    
    return signedParams;  
  }

  private String hmac(String stringToSign) 
  {
    String signature = null;
    byte[] data;
    byte[] rawHmac;
    try {
      data = stringToSign.getBytes(UTF8_CHARSET);
      rawHmac = mac.doFinal(data);
//      Base64 encoder = new Base64();
//      signature = new String(encoder.encode(rawHmac));
// SWOBI: Java 8 at last has a base64 class, so I got rid of one dependency       
      signature = Base64.getEncoder().encodeToString(rawHmac); 

    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(UTF8_CHARSET + " is unsupported!", e);
    }
    return signature;
  }

  private String timestamp() 
  {
    String timestamp = null;
    Calendar cal = Calendar.getInstance();
    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
    timestamp = dfm.format(cal.getTime());
    return timestamp;
  }

  private String canonicalize(SortedMap<String, String> sortedParamMap)
  {
    if (sortedParamMap.isEmpty()) {
      return "";
    }

    StringBuffer buffer = new StringBuffer();
    Iterator<Map.Entry<String, String>> iter = sortedParamMap.entrySet().iterator();

    while (iter.hasNext()) {
      Map.Entry<String, String> kvpair = iter.next();
      buffer.append(percentEncodeRfc3986(kvpair.getKey()));
      buffer.append("=");
      buffer.append(percentEncodeRfc3986(kvpair.getValue()));
      if (iter.hasNext()) {
        buffer.append("&");
      }
    }
    String canonical = buffer.toString();
    return canonical;
  }

  private String percentEncodeRfc3986(String s) 
  {
    String out;
    try {
      out = URLEncoder.encode(s, UTF8_CHARSET)
      .replace("+", "%20")
      .replace("*", "%2A")
      .replace("%7E", "~");
    } catch (UnsupportedEncodingException e) {
      out = s;
    }
    return out;
  }

}