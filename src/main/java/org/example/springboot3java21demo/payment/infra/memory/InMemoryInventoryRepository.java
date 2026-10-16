package org.example.springboot3java21demo.payment.infra.memory;

import org.example.springboot3java21demo.payment.domain.StockSnapshot;
import org.example.springboot3java21demo.payment.repository.InventoryRepository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * 库存仓储的内存实现。
 *
 * <p>用 AtomicReference 加 CAS 自旋，模拟数据库中条件更新的原子语义：
 * 只有当快照未被其他人修改时才写入，失败就重读最新值重算。
 * 这正是乐观锁的原理，所以本类在并发压测下的行为可以代表 MySQL 条件更新的行为。</p>
 */
public class InMemoryInventoryRepository implements InventoryRepository {

    private final ConcurrentHashMap<String, AtomicReference<StockSnapshot>> stocks = new ConcurrentHashMap<>();

    /**
     * 初始化库存，对应 DB 里的 INSERT
     */
    public InMemoryInventoryRepository seed(String skuId, long total) {
        stocks.put(skuId, new AtomicReference<>(new StockSnapshot(skuId, total, 0L, 0L)));
        return this;
    }

    @Override
    public boolean tryReserve(String skuId, int quantity) {
        return update(skuId, snapshot -> snapshot.reserve(quantity)) != null;
    }

    @Override
    public boolean confirm(String skuId, int quantity) {
        return update(skuId, snapshot -> snapshot.confirm(quantity)) != null;
    }

    @Override
    public boolean release(String skuId, int quantity) {
        return update(skuId, snapshot -> snapshot.release(quantity)) != null;
    }

    @Override
    public Optional<StockSnapshot> find(String skuId) {
        AtomicReference<StockSnapshot> ref = stocks.get(skuId);
        return ref == null ? Optional.empty() : Optional.of(ref.get());
    }

    /**
     * CAS 自旋：把「读-改-写」变成原子操作。
     * 返回 null 表示业务上不允许，例如库存不足；抛异常表示非法操作，例如超量释放。
     */
    private StockSnapshot update(String skuId, UnaryOperator<StockSnapshot> mutator) {
        AtomicReference<StockSnapshot> ref = stocks.computeIfAbsent(skuId,
                key -> new AtomicReference<>(new StockSnapshot(key, 0L, 0L, 0L)));
        while (true) {
            StockSnapshot current = ref.get();
            StockSnapshot next = mutator.apply(current);
            if (next == null) {
                return null;
            }
            if (ref.compareAndSet(current, next)) {
                return next;
            }
            // CAS 失败说明有并发修改，重读最新快照后重算，绝不使用过期数据
        }
    }
}
