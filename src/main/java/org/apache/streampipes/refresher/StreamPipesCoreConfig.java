/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.apache.streampipes.refresher;

public class StreamPipesCoreConfig {

  private String userName;

  private String password;

  private String host;

  private String port;

  public StreamPipesCoreConfig() {
    userName = Environment.getENV(ConfigKeys.SP_USERNAME);
    password = Environment.getENV(ConfigKeys.SP_PASSWORD);
    host = Environment.getENV(ConfigKeys.SP_HOST);
    port = Environment.getENV(ConfigKeys.SP_PORT);
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }
}
