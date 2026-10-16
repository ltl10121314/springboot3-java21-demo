package org.example.springboot3java21demo.exercise.reflection;

import java.io.*;

public class CustomClassLoader extends ClassLoader {
    private final String classPath;

    public CustomClassLoader(String classPath) {

        this.classPath = classPath;
    }

    public static void main(String[] args) {

    }

    /**
     * 标准规范：仅重写findClass读取字节码，不破坏双亲委派
     *
     * @param name The <a href="#binary-name">binary name</a> of the class
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String fileName = classPath + File.separator + name.replace(".", File.separator) + ".class";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = fis.read()) != -1) {
                baos.write(b);
            }
            return defineClass(name, baos.toByteArray(), 0, baos.size());
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    /**
     * 如需打破委派，重写loadClass，先自身加载再委托父类（有内存泄漏风险）
     *
     * @param name    The <a href="#binary-name">binary name</a> of the class
     * @param resolve If {@code true} then resolve the class
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException ignored) {
                c = super.loadClass(name, resolve);
            }
        }
        if (resolve) resolveClass(c);
        return c;
    }
}
