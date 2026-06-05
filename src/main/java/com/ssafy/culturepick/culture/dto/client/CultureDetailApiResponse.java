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
public class CultureDetailApiResponse {

    private Body body;

    @Getter
    @NoArgsConstructor
    public static class Body {

        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(localName = "items")
        private List<Item> items;
    }

    @Getter
    @NoArgsConstructor
    public static class Item {

        private Long seq;
        private String title;
        private String startDate;
        private String endDate;
        private String place;
        private String area;
        private String sigungu;
        private String price;
        private String url;
        private String phone;
        private String imgUrl;
        private Double gpsX;
        private Double gpsY;
        private String placeUrl;
        private String placeAddr;
        private Long placeSeq;
    }
}
