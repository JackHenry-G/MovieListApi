package com.goggin.movielist.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbResponse {
    private int page;
    private List<TmdbResponseResult> results;
    private int total_pages;
    private int total_results;
}
