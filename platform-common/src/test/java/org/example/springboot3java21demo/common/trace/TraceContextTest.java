package org.example.springboot3java21demo.common.trace;

import org.example.springboot3java21demo.common.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TraceContextTest {

    @AfterEach
    void clearContext() {
        TraceContext.clear();
    }

    @Test
    @DisplayName("嵌套 traceId 作用域退出后恢复上层上下文")
    void nestedScopesRestorePreviousTraceId() {
        try (TraceContext.Scope outer = TraceContext.open("trace-outer")) {
            assertThat(TraceContext.current()).isEqualTo("trace-outer");
            try (TraceContext.Scope inner = TraceContext.open("trace-inner")) {
                assertThat(TraceContext.current()).isEqualTo("trace-inner");
            }
            assertThat(TraceContext.current()).isEqualTo("trace-outer");
        }
        assertThat(TraceContext.current()).isNull();
    }

    @Test
    @DisplayName("只有入口作用域显式生成 traceId")
    void generatesTraceIdOnlyInsideRequestScope() {
        assertThat(TraceContext.current()).isNull();
        try (TraceContext.Scope ignored = TraceContext.open()) {
            assertThat(TraceContext.current()).matches("[A-Za-z0-9._-]{1,128}");
        }
        assertThat(TraceContext.current()).isNull();

        String generated = TraceContext.newTraceId();
        assertThat(generated).matches("[A-Za-z0-9._-]{1,128}");
        assertThat(TraceContext.current()).isNull();
    }

    @Test
    @DisplayName("traceId 清理非法字符并限制长度")
    void sanitizesTraceId() {
        assertThat(TraceContext.sanitize("  req-123.abc  ")).isEqualTo("req-123.abc");
        assertThat(TraceContext.sanitize("bad trace")).isNull();
        assertThat(TraceContext.sanitize("x".repeat(129))).isNull();
    }

    @Test
    @DisplayName("关联任务只继承已有 traceId，无关任务不生成")
    void relatedTaskOnlyInheritsExistingTraceId() {
        assertThat(TraceContext.openIfPresent(null)).isNull();
        assertThat(TraceContext.current()).isNull();

        try (TraceContext.Scope ignored = TraceContext.openIfPresent("trace-related-task")) {
            assertThat(TraceContext.current()).isEqualTo("trace-related-task");
        }
        assertThat(TraceContext.current()).isNull();
    }
}
