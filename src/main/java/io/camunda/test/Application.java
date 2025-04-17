package io.camunda.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  private final ZeebeClient zeebeClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public Application(ZeebeClient zeebeClient) {
    this.zeebeClient = zeebeClient;
  }

  @PostConstruct
  public void uploadDoc() {
    try (InputStream inputStream = getClass().getClassLoader()
        .getResourceAsStream("5-mb-example-file.pdf")) {
      final var result = zeebeClient.newCreateDocumentBatchCommand()
          .addDocument()
          .content(inputStream)
          .fileName("5-mb-example-file.pdf")
          .done()
          .send()
          .join();
      System.out.println("Document uploaded successfully: " + objectMapper.writeValueAsString(
          result.getCreatedDocuments().getFirst()));

      final InputStream documentCopy = zeebeClient.newDocumentContentGetRequest(result.getCreatedDocuments().getFirst())
          .send()
          .join();

      // write to file
      java.nio.file.Files.copy(documentCopy, java.nio.file.Paths.get("copy-5-mb-example-file.pdf"));

    } catch (Exception e) {
      System.err.println("Failed to upload document: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
