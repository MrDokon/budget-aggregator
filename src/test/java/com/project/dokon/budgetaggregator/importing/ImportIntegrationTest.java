package com.project.dokon.budgetaggregator.importing;

import com.project.dokon.budgetaggregator.config.TestContainersConfiguration;
import com.project.dokon.budgetaggregator.core.model.Transaction;
import com.project.dokon.budgetaggregator.core.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestContainersConfiguration.class)
public class ImportIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    void shouldImportCsvAndReturnStatistics() {

        restTestClient.post()
                .uri("/api/v1/imports")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(importTestData())
                .exchange()
                .expectStatus()
                .isAccepted();

        await()
                .atMost(15, SECONDS)
                .pollInterval(500, MILLISECONDS)
                .untilAsserted(() -> {
                    List<Transaction> transactions = transactionRepository.findAll();
                    assertThat(transactions).hasSize(10);
                    assertThat(transactions)
                            .extracting(Transaction::transactionId)
                            .contains("tx001", "tx010");
                });

        restTestClient.get()
                .uri("/api/v1/statistics?year=2024&month=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.year").isEqualTo(2024)
                .jsonPath("$.month").isEqualTo(1)
                .jsonPath("$.stats.length()").isEqualTo(4)
                .jsonPath("$.stats[?(@.category == 'Rozrywka')].totalAmount").isEqualTo(612.97)
                .jsonPath("$.stats[?(@.category == 'Rozrywka')].transactionCount").isEqualTo(5)
                .jsonPath("$.stats[?(@.category == 'Spozywcze')].totalAmount").isEqualTo(150.50);
    }

    private MultiValueMap<String, Object> importTestData() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ClassPathResource("import.csv"));
        return body;
    }


}
