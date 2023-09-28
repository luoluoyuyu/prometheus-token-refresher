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

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Refresher {

  private static final Logger LOG = LoggerFactory.getLogger(Refresher.class.getCanonicalName());
  private static final String Login_URL = "/streampipes-backend/api/v2/auth/login";
  private static final String SP_Health_URL = "/streampipes-backend/api/v2/setup/configured";
  private static final String Reload_URL = "/-/reload";
  private static final String Prometheus_Health_URL = "/metrics";
  private static final long maxRetryTime = TimeUnit.MINUTES.toMillis(1);
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final PrometheusConfig prometheusConfig = new PrometheusConfig();
  private static final StreamPipesCoreConfig streamPipesCoreConfig = new StreamPipesCoreConfig();
  private static final OkHttpClient client = new OkHttpClient().newBuilder().build();


  public static void main(String[] args) {
    LOG.info("start");
    Request loginRequest = buildLoginRequest();

    Request SPHealth = buildStreampipesHealthRequest();
    Request PrometheusHealth = buildPrometheusHealthRequest();
    Request Reload = buildPrometheusReloadRequest();
    do {
      if (!checkHealth(SPHealth)) {
        LOG.info("StreamPipes is not healthy.");
        break;
      }

      JwtAuthenticationResponse jwt = processLoginResponse(loginRequest);
      if (jwt != null) {
        // Retrieve the accessToken from the JwtAuthenticationResponse object
        String accessToken = jwt.getAccessToken();
        // Write accessToken to the TOKEN_FILE
        boolean writeSuccess = writeAccessTokenToFile(accessToken);
        if (!writeSuccess) {
          LOG.info("The file does not exist, unable to write accessToken.");
          break;
        }
      }

      if (!checkHealth(PrometheusHealth)) {
        LOG.info("Prometheus is not healthy.");
        break;
      }

      if (!reload(Reload)) {
        LOG.error("Reload request to Prometheus failed.");
        break;
      }

      try {
        TimeUnit.SECONDS.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

    } while (true);
  }


  private static Request buildLoginRequest() {
    MediaType mediaType = MediaType.parse("application/json");
    LoginRequest loginRequest = new LoginRequest(streamPipesCoreConfig.getUserName(), streamPipesCoreConfig.getPassword());
    String jsonRequestBody = null;
    try {
      jsonRequestBody = objectMapper.writeValueAsString(loginRequest);
    } catch (Exception e) {
      e.printStackTrace();
    }
    RequestBody body = RequestBody.create(mediaType, jsonRequestBody);
    return new Request.Builder()
      .url("http://" + streamPipesCoreConfig.getHost() + ":" + streamPipesCoreConfig.getPort() + Login_URL)
      .method("POST", body)
      .addHeader("Content-Type", "application/json")
      .build();
  }


  private static Request buildStreampipesHealthRequest() {
    return new Request.Builder()
      .url("http://" + streamPipesCoreConfig.getHost() + ":" + streamPipesCoreConfig.getPort() + SP_Health_URL)
      .method("GET", null)
      .addHeader("Content-Type", "application/json")
      .build();
  }

  private static Request buildPrometheusHealthRequest() {
    return new Request.Builder()
      .url("http://" + prometheusConfig.getHost() + ":" + prometheusConfig.getPort() + Prometheus_Health_URL)
      .method("GET", null)
      .addHeader("Content-Type", "application/json")
      .build();
  }

  private static Request buildPrometheusReloadRequest() {
    OkHttpClient client = new OkHttpClient().newBuilder()
      .build();
    MediaType mediaType = MediaType.parse("text/plain");
    RequestBody body = RequestBody.create(mediaType, "");
    return  new Request.Builder()
      .url("http://" + prometheusConfig.getHost() + ":" + prometheusConfig.getPort() + Reload_URL)
      .method("POST", body)
      .build();
  }

  private static boolean checkHealth(Request healthRequest) {
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime <= maxRetryTime) {
      try {
        Response response = client.newCall(healthRequest).execute();
        if (response.isSuccessful()) {
          return true;
        }
        TimeUnit.SECONDS.sleep(10);
      } catch (IOException | InterruptedException e) {
        LOG.error("Request failed.");
      }
    }
    return false;
  }

  private static boolean reload(Request healthRequest) {
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime <= maxRetryTime) {
      try {
        Response response = client.newCall(healthRequest).execute();
        if (response.isSuccessful()) {
          return true;
        }
        TimeUnit.SECONDS.sleep(10);
      } catch (IOException | InterruptedException e) {
        LOG.error("Request failed.");
      }
    }
    return false;
  }

  // Process the login request response
  private static JwtAuthenticationResponse processLoginResponse(Request loginRequest) {
    try {
      Response response = client.newCall(loginRequest).execute();
      if (response.isSuccessful()) {
        // Parse the JSON response body
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(Objects.requireNonNull(response.body()).string(), JwtAuthenticationResponse.class);
      } else {
        LOG.info("Login request failed.");
      }
    } catch (IOException e) {
      e.printStackTrace();
      LOG.info("Login request failed.");
    }
    return null;
  }

  // Write accessToken to the file
  private static boolean writeAccessTokenToFile(String accessToken) {
    try {
      Path filePath = Path.of(prometheusConfig.getTokenFile());
      if (!Files.exists(filePath)) {
        // If the file does not exist, create the file
        Files.createFile(filePath);
        LOG.info("create token file");
      }
      // Clear the file content and write accessToken
      Files.write(filePath, (accessToken).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }


}
