package io.datahubproject.openapi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.datahubproject.metadata.context.OperationContext;
import io.datahubproject.openapi.v2.models.BatchGetUrnRequestV2;
import io.datahubproject.openapi.v2.models.BatchGetUrnResponseV2;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;

/** TODO: This should be autogenerated from our own OpenAPI */
@Slf4j
public class OpenApiClient {

  private final CloseableHttpClient httpClient;
  private final String gmsHost;
  private final int gmsPort;
  private final boolean useSsl;
  @Getter private final OperationContext systemOperationContext;

  private static final String OPENAPI_PATH = "/openapi/v2/entity/batch/";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public OpenApiClient(
      String gmsHost, int gmsPort, boolean useSsl, OperationContext systemOperationContext) {
    this.gmsHost = gmsHost;
    this.gmsPort = gmsPort;
    this.useSsl = useSsl;
    httpClient = HttpClientBuilder.create().build();
    this.systemOperationContext = systemOperationContext;
  }

  public BatchGetUrnResponseV2 getBatchUrnsSystemAuth(
      String entityName, BatchGetUrnRequestV2 request) {
    return getBatchUrns(
        entityName,
        request,
        systemOperationContext.getSystemAuthentication().get().getCredentials());
  }

  public BatchGetUrnResponseV2 getBatchUrns(
      String entityName, BatchGetUrnRequestV2 request, String authCredentials) {
    String url =
        (useSsl ? "https://" : "http://") + gmsHost + ":" + gmsPort + OPENAPI_PATH + entityName;
    HttpPost httpPost = new HttpPost(url);
    httpPost.setHeader(HttpHeaders.AUTHORIZATION, authCredentials);
    try {
      httpPost.setEntity(
          new StringEntity(
              OBJECT_MAPPER.writeValueAsString(request), ContentType.APPLICATION_JSON));
      httpPost.setHeader("Content-type", "application/json");
      return httpClient.execute(httpPost, OpenApiClient::mapResponse);
    } catch (IOException e) {
      log.error("Unable to execute Batch Get request for urn: " + request.getUrns(), e);
      throw new RuntimeException(e);
    }
  }

  private static BatchGetUrnResponseV2 mapResponse(ClassicHttpResponse response) {
    BatchGetUrnResponseV2 serializedResponse;
    try {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      InputStream contentStream = response.getEntity().getContent();
      byte[] buffer = new byte[1024];
      int length = contentStream.read(buffer);
      while (length > 0) {
        result.write(buffer, 0, length);
        length = contentStream.read(buffer);
      }
      serializedResponse =
          OBJECT_MAPPER.readValue(
              result.toString(StandardCharsets.UTF_8), BatchGetUrnResponseV2.class);
    } catch (IOException e) {
      log.error("Wasn't able to convert response into expected type.", e);
      throw new RuntimeException(e);
    }
    return serializedResponse;
  }
}
