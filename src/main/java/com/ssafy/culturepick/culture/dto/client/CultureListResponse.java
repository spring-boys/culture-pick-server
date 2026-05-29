package com.ssafy.culturepick.culture.dto.client;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JacksonXmlRootElement(localName = "response")
public class CultureListResponse {

    private Body body; // 필드가 없으면 <body> 태그 무시하고 지나간대

    @Getter
    @NoArgsConstructor
    public static class Body {

        private int totalCount;

        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(localName = "items")
        private List<Item> items;
    }

    @Getter
    @NoArgsConstructor
    public static class Item {

        private Long seq;
        private String serviceName;
        private String title;
        private String startDate;
        private String endDate;
        private String place;
        private String area;
        private String sigungu;
        private String thumbnail;
        private Double gpsX;
        private Double gpsY;
    }
}
