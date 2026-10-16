package org.example.springboot3java21demo.exercise.reflection;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.exercise.domain.User;
import org.junit.Test;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

interface Hello {
    /**
     * 测试
     *
     * @param name
     */
    void morning(String name);
}

interface UserService {
    void save();
}

class UserServiceImpl implements UserService {
    @Override
    public void save() {
        System.out.println("保存用户");
    }
}

class StudentService {
    public void save() {
        System.out.println("保存用户");
    }
}

/**
 * 反射测试
 */
@Slf4j
public class ReflectionTest {

    public static void main(String[] args) {

    }

    @Test
    public void dynamicProxyTest() {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                log.info(method.toString());
                if ("morning".equals(method.getName())) {
                    log.info("Good morning, {}", args[0]);
                }
                return null;
            }
        };
        Hello hello = (Hello) Proxy.newProxyInstance(
                // 传入ClassLoader
                Hello.class.getClassLoader(),
                // 传入要实现的接口
                new Class[]{Hello.class},
                // 传入处理调用方法的InvocationHandler
                handler);
        hello.morning("Bob");
    }

    @Test
    public void jdkDynamicProxy() {
        UserService target = new UserServiceImpl();
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(
                    Object proxy,
                    Method method,
                    Object[] args) throws Throwable {
                log.info("jdkDynamicProxy方法执行前");
                Object result = method.invoke(target, args);
                log.info("jdkDynamicProxy方法执行后");
                return result;
            }
        };
        UserService proxy = (UserService) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                handler
        );
        proxy.save();
    }

    @Test
    public void reflectionTest() {
        User user = new User();
        user.setId("1");
        user.setAge("12");
        user.setAddress("天津");
        Class<? extends User> aClass = user.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields) {
            log.info(String.valueOf(field));
        }
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            log.info(String.valueOf(method));
        }
    }

    @Test
    public void cglibPoxyTest() {
        Enhancer enhancer = new Enhancer();
        // 设置目标类
        enhancer.setSuperclass(StudentService.class);
        // 设置方法拦截器
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(
                    Object obj,
                    Method method,
                    Object[] args,
                    MethodProxy methodProxy) throws Throwable {
                log.info("cglibPoxyTest方法执行前");
                // 调用父类的真实方法实现
                Object result = methodProxy.invokeSuper(obj, args);
                log.info("cglibPoxyTest方法执行后");
                return result;
            }
        });
        // 创建代理对象
        StudentService proxy = (StudentService) enhancer.create();
        proxy.save();
    }

    @Test
    public void test1() {
        Class<?> aClass = null;
        try {
            aClass = Class.forName("org.example.springboot3java21demo.exercise.test.BillData");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Field[] fields = aClass.getFields();
        for (Field field : fields) {
            log.error(field.toString());
        }
        log.error(aClass.toString());
    }
}

