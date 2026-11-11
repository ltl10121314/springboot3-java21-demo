package org.example.springboot3java21demo.orderpay.web;

import org.example.springboot3java21demo.orderpay.domain.SearchOrderDocument;

import java.util.List;

public record SearchResult(long total, List<SearchOrderDocument> items) {
}
