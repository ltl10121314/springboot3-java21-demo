package org.example.springboot3java21demo.exercise.function;

import org.example.springboot3java21demo.exercise.domain.Item;
import org.example.springboot3java21demo.exercise.domain.User;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionExample {
    private static final Logger log = LoggerFactory.getLogger(FunctionExample.class);

    @Test
    public void test7() {
        Function<User, String> keyFunc = User::getName;
        BiConsumer<Item, User> consumer = (item, user) -> {
            item.setCode(user.getId());
            item.setName(user.getName());
        };
        User user1 = new User("1", "a", "爱");
        Item item1 = new Item();
        log.error(user1.toString());
        log.error(item1.toString());
        consumer.accept(item1, user1);
        log.error(user1.toString());
        log.error(item1.toString());
        log.error(keyFunc.apply(user1));
        BiFunction<Item, User, String> biFunction = (item2, user) -> {
            String code = item2.getCode();
            String name = user.getName();
            return code + "_" + name;
        };
        String result = biFunction.apply(item1, user1);
        log.error(result);
    }
}
