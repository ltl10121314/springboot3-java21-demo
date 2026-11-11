package org.example.springboot3java21demo.orderpay.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.orderpay.config.OrderPayProperties;
import org.example.springboot3java21demo.orderpay.domain.OrderRecord;
import org.example.springboot3java21demo.orderpay.domain.SearchOrderDocument;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
@Slf4j
public class OrderSearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final OrderPayProperties properties;

    public OrderSearchService(ElasticsearchClient elasticsearchClient, OrderPayProperties properties) {
        this.elasticsearchClient = elasticsearchClient;
        this.properties = properties;
    }

    public void index(OrderRecord order) {
        SearchOrderDocument document = SearchOrderDocument.builder()
                .orderNo(order.orderNo())
                .requestId(order.requestId())
                .userId(order.userId())
                .skuId(order.skuId())
                .skuName(order.skuName())
                .totalAmount(order.totalAmount())
                .quantity(order.quantity())
                .status(order.status().name())
                .paymentNo(order.paymentNo())
                .createdAt(order.createdAt().toString())
                .updatedAt(order.updatedAt().toString())
                .paidAt(order.paidAt() == null ? null : order.paidAt().toString())
                .build();
        try {
            elasticsearchClient.index(request -> request
                    .index(properties.getSearch().getIndex())
                    .id(order.orderNo())
                    .document(document)
                    .refresh(Refresh.WaitFor));
            log.debug("订单索引写入成功 orderNo={}, status={}, index={}",
                    order.orderNo(), order.status(), properties.getSearch().getIndex());
        } catch (IOException ex) {
            throw new UncheckedIOException("写入 Elasticsearch 失败", ex);
        }
    }

    public SearchPage search(String keyword, String status, int page, int size) {
        long startedAt = System.nanoTime();
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (StringUtils.hasText(status)) {
            String normalizedStatus = status.trim().toUpperCase();
            bool.filter(filter -> filter.match(match -> match
                    .field("status")
                    .query(normalizedStatus)));
        }
        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim();
            bool.must(must -> must.multiMatch(multi -> multi
                    .query(normalizedKeyword)
                    .fields(List.of("orderNo^3", "requestId^2", "userId", "skuName"))));
        }
        Query query = !StringUtils.hasText(keyword) && !StringUtils.hasText(status)
                ? Query.of(builder -> builder.matchAll(match -> match))
                : Query.of(builder -> builder.bool(bool.build()));
        try {
            SearchResponse<SearchOrderDocument> response = elasticsearchClient.search(request -> request
                    .index(properties.getSearch().getIndex())
                    .from(safePage * safeSize)
                    .size(safeSize)
                    .query(query), SearchOrderDocument.class);
            List<SearchOrderDocument> items = response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            long total = response.hits().total() == null
                    ? items.size()
                    : response.hits().total().value();
            log.debug("Elasticsearch 检索完成 keyword={}, status={}, total={}, page={}, size={}, elapsedMs={}",
                    keyword, status, total, safePage, safeSize, elapsedMillis(startedAt));
            return new SearchPage(total, items);
        } catch (co.elastic.clients.elasticsearch._types.ElasticsearchException ex) {
            if (ex.status() == 404) {
                log.debug("Elasticsearch 索引尚不存在，返回空结果 index={}",
                        properties.getSearch().getIndex());
                return new SearchPage(0, List.of());
            }
            throw ex;
        } catch (IOException ex) {
            throw new UncheckedIOException("查询 Elasticsearch 失败", ex);
        }
    }

    public record SearchPage(long total, List<SearchOrderDocument> items) {
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
