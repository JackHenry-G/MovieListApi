package com.goggin.movielist.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/***
 * Represents a place found from the Google API search
 */
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    private String websiteUri;
    private DisplayName displayName;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisplayName {
        private String text;
        private String languageCode;
    }
}
