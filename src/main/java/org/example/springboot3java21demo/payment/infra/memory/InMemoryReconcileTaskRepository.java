package org.example.springboot3java21demo.payment.infra.memory;

import org.example.springboot3java21demo.payment.domain.ReconcileTask;
import org.example.springboot3java21demo.payment.repository.ReconcileTaskRepository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对账工单的内存实现。
 */
public class InMemoryReconcileTaskRepository implements ReconcileTaskRepository {

    private final ConcurrentHashMap<String, ReconcileTask> tasks = new ConcurrentHashMap<>();

    @Override
    public void save(ReconcileTask task) {
        tasks.put(task.taskId(), task);
    }

    @Override
    public List<ReconcileTask> findByBizDate(String bizDate) {
        return tasks.values().stream()
                .filter(task -> bizDate.equals(task.bizDate()))
                .sorted(Comparator.comparingLong(ReconcileTask::createdAt))
                .toList();
    }

    @Override
    public List<ReconcileTask> findOpen() {
        return tasks.values().stream()
                .filter(task -> task.status() == ReconcileTask.Status.OPEN)
                .sorted(Comparator.comparingLong(ReconcileTask::createdAt))
                .toList();
    }
}
