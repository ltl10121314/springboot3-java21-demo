package org.example.springboot3java21demo.payment.infra.memory;

import org.example.springboot3java21demo.payment.domain.ChannelTradeBill;
import org.example.springboot3java21demo.payment.repository.ChannelBillRepository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 渠道对账单的内存实现。
 */
public class InMemoryChannelBillRepository implements ChannelBillRepository {

    private final CopyOnWriteArrayList<ChannelTradeBill> bills = new CopyOnWriteArrayList<>();

    @Override
    public void saveAll(List<ChannelTradeBill> newBills) {
        bills.addAll(newBills);
    }

    @Override
    public List<ChannelTradeBill> findByBizDate(String bizDate) {
        return bills.stream().filter(bill -> bizDate.equals(bill.bizDate())).toList();
    }
}
