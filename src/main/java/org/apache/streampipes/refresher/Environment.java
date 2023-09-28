package org.apache.streampipes.refresher;

import java.util.Map;

public class Environment {

  private static Map<String,String> envs=System.getenv();


  public static String getENV(String key){
    return envs.get(key);

  }
}
