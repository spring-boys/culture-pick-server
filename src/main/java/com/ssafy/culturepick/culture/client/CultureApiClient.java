package com.ssafy.culturepick.culture.client;

import com.ssafy.culturepick.culture.dto.client.CultureDetailApiResponse;
import com.ssafy.culturepick.culture.dto.client.CultureListApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class CultureApiClient {

    private final RestClient restClient;

    @Value("${culture.api.base-url}")
    private String baseUrl;

    @Value("${culture.api.service-key}")
    private String serviceKey;

    public CultureListApiResponse getList() {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/period2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("from", "20260101")
                .queryParam("to", "20261231")
                .queryParam("numOfrows", 100000)
                .toUriString();

        return restClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .body(CultureListApiResponse.class);
    }

    public CultureDetailApiResponse getDetail(Long seq) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/detail2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("seq", seq)
                .toUriString();

        return restClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .body(CultureDetailApiResponse.class);
    }
}
