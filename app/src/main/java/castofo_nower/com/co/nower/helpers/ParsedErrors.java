package castofo_nower.com.co.nower.helpers;

import java.util.Map;

public interface ParsedErrors {

  public void notifyParsedErrors(String action,
                                 Map<String, String> errorsMessages);
}
