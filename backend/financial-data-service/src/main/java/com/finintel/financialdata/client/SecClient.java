package com.finintel.financialdata.client;

import com.finintel.financialdata.config.SecApiProperties;
import com.finintel.financialdata.dto.SecCompanyTickerDto;
import com.finintel.financialdata.exception.ResourceNotFoundException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;

@Component
public class SecClient {

    private static final String SEC_COMPANY_TICKERS_CACHE_KEY =
            "sec:company-tickers";

    private static final String SEC_COMPANY_FACTS_CACHE_KEY_PREFIX =
            "sec:company-facts:cik:";

    private static final Duration COMPANY_TICKERS_TTL = Duration.ofHours(24);

    private static final Duration COMPANY_FACTS_TTL = Duration.ofHours(6);

    private final RestClient restClient;
    private final SecApiProperties secApiProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public SecClient(
            RestClient restClient,
            SecApiProperties secApiProperties,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.secApiProperties = secApiProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public SecCompanyTickerDto findCompanyByTicker(String ticker) {
        Map<String, SecCompanyTickerDto> tickerMap = getCompanyTickerMap();

        if (tickerMap.isEmpty()) {
            throw new ResourceNotFoundException("SEC company ticker mapping is empty");
        }

        return tickerMap.values()
                .stream()
                .filter(item -> item.ticker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticker not found in SEC company ticker mapping: " + ticker
                ));
    }

    public JsonNode getCompanyFacts(String cik) {
        String cacheKey = SEC_COMPANY_FACTS_CACHE_KEY_PREFIX + cik;

        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);

        if (cachedJson != null) {
            System.out.println("SEC company facts cache hit for CIK: " + cik);
            return parseJsonNode(cachedJson);
        }

        System.out.println("SEC company facts cache miss for CIK: " + cik);

        String url = secApiProperties.companyFactsUrlTemplate().formatted(cik);

        String json = restClient.get()
                .uri(url)
                .header(HttpHeaders.USER_AGENT, secApiProperties.userAgent())
                .retrieve()
                .body(String.class);

        if (json == null || json.isBlank()) {
            throw new ResourceNotFoundException(
                    "SEC company facts response is empty for CIK: " + cik
            );
        }

        stringRedisTemplate.opsForValue().set(
                cacheKey,
                json,
                COMPANY_FACTS_TTL
        );

        return parseJsonNode(json);
    }

    public String formatCik(Integer cikStr) {
        return String.format("%010d", cikStr);
    }

    private Map<String, SecCompanyTickerDto> getCompanyTickerMap() {
        String cachedJson = stringRedisTemplate.opsForValue()
                .get(SEC_COMPANY_TICKERS_CACHE_KEY);

        if (cachedJson != null) {
            System.out.println("SEC company tickers cache hit");
            return parseCompanyTickerMap(cachedJson);
        }

        System.out.println("SEC company tickers cache miss");

        String json = restClient.get()
                .uri(secApiProperties.companyTickersUrl())
                .header(HttpHeaders.USER_AGENT, secApiProperties.userAgent())
                .retrieve()
                .body(String.class);

        if (json == null || json.isBlank()) {
            throw new ResourceNotFoundException("SEC company ticker mapping response is empty");
        }

        stringRedisTemplate.opsForValue().set(
                SEC_COMPANY_TICKERS_CACHE_KEY,
                json,
                COMPANY_TICKERS_TTL
        );

        return parseCompanyTickerMap(json);
    }

    private Map<String, SecCompanyTickerDto> parseCompanyTickerMap(String json) {
        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<>() {
                    }
            );
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to parse SEC company ticker mapping",
                    exception
            );
        }
    }

    private JsonNode parseJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Failed to parse SEC company facts JSON",
                    exception
            );
        }
    }
}