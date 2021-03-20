package de.peeeq.wurstscript.translation.imoptimizer;

import de.peeeq.wurstscript.utils.Debug;
import java.io.FileNotFoundException;

/**
 * This will be used to generate unique Strings which aren't named like the ones in the restricted
 * list. Main use for compressing names.
 *
 * @author Frotty
 */
public class NameGenerator {
  /** The given charmap */
  private final String charmapFirst = "wurstiScoOlbypeqandfRTYGghFkjxvmQWEZUIPADHJKLXCVBNM";

  private final String charmap = charmapFirst + "3142567890";
  private final String charmapMid = charmap + "_";

  private String TEcharmap = "wurstqeiopadfghjklyxcvbnm";
  /** A counter */
  private int currentId = 0;

  private int TEId = 0;
  /** length of charmap */
  private int lengthFirst;

  private int length;
  private int lengthMid;
  private int TElength = 25;

  /**
   * Creates a new NameGenerator
   *
   * @throws FileNotFoundException
   */
  public NameGenerator() {
    checkCharmap(charmapFirst);
    checkCharmap(charmap);
    checkCharmap(charmapMid);
    length = charmap.length();
    lengthMid = charmapMid.length();
    lengthFirst = charmapFirst.length();
  }

  private void checkCharmap(String c) {
    for (int i = 0; i < c.length(); i++) {
      for (int j = i + 1; j < c.length(); j++) {
        if (c.charAt(i) == c.charAt(j)) {
          throw new Error("Charmap contains letter " + c.charAt(i) + " twice. -- " + c);
        }
      }
    }
  }

  /**
   * Get a token
   *
   * @return A (for this Namegenrator) unique token
   */
  public String getToken() {
    int id = currentId++;
    StringBuilder b = new StringBuilder();
    b.append(charmapFirst.charAt(id % lengthFirst));
    if ((id /= lengthFirst) != 0) {
      do {
        if (id > lengthMid) {
          b.append(charmapMid.charAt((id - 1) % lengthMid));
        } else {
          b.append(charmap.charAt((id - 1) % length));
        }
      } while ((id /= length) != 0);
    }

    return b.toString();
  }

  /**
   * Get a token that is cross checked with the Restricted names
   *
   * @return A checked, unique token
   */
  public String getUniqueToken() {
    String s = getToken();
    while (RestrictedCompressedNames.contains(s)) {
      Debug.println(s + "is restricted");
      // Wishful thinking, but normally this should work
      // there are only a handful of restricted names anyway.
      s = getToken();
    }

    return s;
  }

  /**
   * Get a token that can be used for TE and TRVE since its only lowercase. To be unique these start
   * with "z"
   *
   * @return A checked, unique token, lowercase, starting with z
   */
  public String getTEToken() {
    int id = TEId++;
    StringBuilder b = new StringBuilder("z");
    do {
      b.append(TEcharmap.charAt(id % TElength));
    } while ((id /= TElength) != 0);

    return b.toString();
  }
}
