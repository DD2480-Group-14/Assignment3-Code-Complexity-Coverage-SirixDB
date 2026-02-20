package io.sirix.utils;

import io.brackit.query.atomic.QNm;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * This class provides convenience operations for XML-specific character operations.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class XMLToken {
  /** Hidden constructor. */
  private XMLToken() {
    throw new AssertionError("May never be instantiated!");
  }

  private static final Set<Integer> SINGLE_CHARS = Set.of('_');

  private static final List<CharRange> VALID_RANGES = List.of(
    new CharRange('A', 'Z'),
    new CharRange('a', 'z'),

    new CharRange(0xC0, 0xD6),
    new CharRange(0xD8, 0xF6),
    new CharRange(0xF8, 0x2FF),

    new CharRange(0x370, 0x37D),
    new CharRange(0x37F, 0x1FFF),
    new CharRange(0x200C, 0x200D),
    new CharRange(0x2070, 0x218F),
    new CharRange(0x2C00, 0x2EFF),
    new CharRange(0x3001, 0xD7FF),
    new CharRange(0xF900, 0xFDCF),
    new CharRange(0xFDF0, 0xFFFD),
    new CharRange(0x10000, 0xEFFFF)
  );

  /**
   * Checks if the specified character is a valid XML character.
   *
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean valid(final int ch) {
    return ch >= 0x20 && ch <= 0xD7FF || ch == 0xA || ch == 0x9 || ch == 0xD
        || ch >= 0xE000 && ch <= 0xFFFD || ch >= 0x10000 && ch <= 0x10ffff;
  }

  /**
   * Checks if the specified character is a name start character, as required e.g. by QName and
   * NCName using a lookup table
   *
   * @param ch character
   * @return result of check is true if valid NC start character
   */
  public static boolean isNCStartChar(final int ch) {
    if (SINGLE_CHARS.contains(ch))(
      return true;
    )

    for (CharRange range : VALID_RANGES) {
      if (range contains(ch)){
        return true;
      }
    }

    return false;
  }



  /**
   * Checks if the specified character is an XML letter.
   *
   * @param ch character
   * @return result of check
   */
  public static boolean isNCChar(final int ch) {
    return isNCStartChar(ch) || (ch < 0x100
        ? digit(ch) || ch == '-' || ch == '.' || ch == 0xB7
        : ch >= 0x300 && ch <= 0x36F || ch == 0x203F || ch == 0x2040);
  }

  /**
   * Checks if the specified character is an XML first-letter.
   *
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean isStartChar(final int ch) {
    return isNCStartChar(ch) || ch == ':';
  }

  /**
   * Checks if the specified character is an XML letter.
   *
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean isChar(final int ch) {
    return isNCChar(ch) || ch == ':';
  }

  /**
   * Checks if the specified token is a valid NCName.
   *
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isNCName(final byte[] v) {
    final int l = v.length;
    return l != 0 && ncName(v, 0) == l;
  }

  /**
   * Checks if the specified token is a valid name.
   *
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isName(final byte[] v) {
    final int l = v.length;
    for (int i = 0; i < l; i += cl(v, i)) {
      final int c = cp(v, i);
      if (i == 0
          ? !isStartChar(c)
          : !isChar(c))
        return false;
    }
    return l != 0;
  }

  /**
   * Checks if the specified token is a valid NMToken.
   *
   * @param v value to be checked
   * @return result of check
   */
  public static boolean isNMToken(final byte[] v) {
    final int l = v.length;
    for (int i = 0; i < l; i += cl(v, i))
      if (!isChar(cp(v, i)))
        return false;
    return l != 0;
  }

  /**
   * Checks if the specified token is a valid QName.
   *
   * @param val value to be checked
   * @return result of check
   */
  public static boolean isQName(final byte[] val) {
    final int l = val.length;
    if (l == 0)
      return false;
    final int i = ncName(val, 0);
    if (i == l)
      return true;
    if (i == 0 || val[i] != ':')
      return false;
    final int j = ncName(val, i + 1);
    if (j == i + 1 || j != l)
      return false;
    return true;
  }

  /**
   * Checks if the specified token is a valid {@link QName}.
   *
   * @param name {@link QName} reference to check
   * @return {@code true} if it's valid, {@code false} otherwise
   * @throws NullPointerException if {@code name} is {@code null}
   */
  public static boolean isValidQName(final QNm name) {
    final String localPart = requireNonNull(name).getLocalName();
    if (!localPart.isEmpty() && !isNCName(localPart.getBytes())) {
      return false;
    }
    final String prefix = name.getPrefix();
    if (!prefix.isEmpty() && !isNCName(prefix.getBytes())) {
      return false;
    }
    // if (!isUrl(name.getNamespaceURI())) {
    // return false;
    // }
    return true;
  }

  /**
   * Determines if the provided {@code namespaceURI} is a valid {@code URI}.
   *
   * @param namespaceURI the {@code URI} to check
   * @return {@code true} if {@code namespaceURI} is valid, {@code false} otherwise
   */
  public static boolean isUrl(final String namespaceURI) {
    // NamespaceURI is never null.
    try {
      new URL(namespaceURI).toURI();
    } catch (final MalformedURLException | URISyntaxException e) {
      return false;
    }
    return true;
  }

  /**
   * Returns the codepoint (unicode value) of the specified token, starting at the specified
   * position. Returns a unicode replacement character for invalid values.
   *
   * @param token token
   * @param pos character position
   * @return current character
   */
  public static int cp(final byte[] token, final int pos) {
    // 0xxxxxxx
    final byte v = token[pos];
    if ((v & 0xFF) < 192)
      return v & 0xFF;
    // number of bytes to be read
    final int vl = cl(v);
    if (pos + vl > token.length)
      return 0xFFFD;
    // 110xxxxx 10xxxxxx
    if (vl == 2)
      return (v & 0x1F) << 6 | token[pos + 1] & 0x3F;
    // 1110xxxx 10xxxxxx 10xxxxxx
    if (vl == 3)
      return (v & 0x0F) << 12 | (token[pos + 1] & 0x3F) << 6 | token[pos + 2] & 0x3F;
    // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
    return (v & 0x07) << 18 | (token[pos + 1] & 0x3F) << 12 | (token[pos + 2] & 0x3F) << 6
        | token[pos + 3] & 0x3F;
  }

  /**
   * Checks the specified token as an NCName.
   *
   * @param v value to be checked
   * @param p start position
   * @return end position
   */
  private static int ncName(final byte[] v, final int p) {
    final int l = v.length;
    for (int i = p; i < l; i += cl(v, i)) {
      final int c = cp(v, i);
      if (i == p
          ? !isNCStartChar(c)
          : !isNCChar(c))
        return i;
    }
    return l;
  }

  /**
   * Returns the codepoint length of the specified byte.
   *
   * @param first first character byte
   * @return character length
   */
  public static int cl(final byte first) {
    return first >= 0
        ? 1
        : CHLEN[first >> 4 & 0xF];
  }

  /*** Character lengths. */
  private static final int[] CHLEN = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4};

  /**
   * Returns the codepoint length of the specified byte.
   *
   * @param token token
   * @param pos character position
   * @return character length
   */
  public static int cl(final byte[] token, final int pos) {
    return cl(token[pos]);
  }

  /**
   * Checks if the specified character is a digit (0 - 9).
   *
   * @param ch the letter to be checked
   * @return result of comparison
   */
  public static boolean digit(final int ch) {
    return ch >= '0' && ch <= '9';
  }

  /**
   * Escape characters not allowed in attribute values.
   *
   * @param value the string value to escape
   * @return escaped value
   * @throws NullPointerException if {@code pValue} is {@code null}
   */
  public static String escapeAttribute(final String value) {
    requireNonNull(value);
    final StringBuilder escape = new StringBuilder();
    for (final char i : value.toCharArray()) {
      switch (i) {
        case '&':
          escape.append("&amp;");
          break;
        case '<':
          escape.append("&lt;");
          break;
        case '>':
          escape.append("&gt;");
          break;
        case '"':
          escape.append("&quot;");
          break;
        case '\'':
          escape.append("&apos;");
          break;
        default:
          escape.append(i);
      }
    }
    return escape.toString();
  }

  /**
   * Escape characters not allowed text content.
   *
   * @param value the string value to escape
   * @return escaped value
   * @throws NullPointerException if {@code value} is {@code null}
   */
  public static String escapeContent(final String value) {
    requireNonNull(value);
    final StringBuilder escape = new StringBuilder();
    for (final char i : value.toCharArray()) {
      switch (i) {
        case '&':
          escape.append("&amp;");
          break;
        case '<':
          escape.append("&lt;");
          break;
        case '>':
          escape.append("&gt;");
          break;
        default:
          escape.append(i);
      }
    }
    return escape.toString();
  }
}
