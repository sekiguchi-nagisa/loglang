package loglang.jvm;

import loglang.Config;
import loglang.misc.FatalError;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * for user defined class loading. not thread safe.
 */
public class ByteCodeLoader extends ClassLoader {

    /**
     * must be fully qualified binary name(contains . ).
     */
    private final String allowedPackageName;

    /**
     * contains byte code(require java class specification).
     * key is fully qualified binary class name(contains . ).
     */
    private final Map<String, byte[]> byteCodeMap;

    public ByteCodeLoader(String packageName) {
        super();
        this.allowedPackageName = toBinaryName(packageName);
        this.byteCodeMap = new HashMap<>();
    }

    /**
     * used for child class loader creation.
     * @param classLoader
     */
    protected ByteCodeLoader(ByteCodeLoader classLoader) {
        super(classLoader);
        this.allowedPackageName = classLoader.allowedPackageName;
        this.byteCodeMap = new HashMap<>();
    }

    @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] byteCode = this.byteCodeMap.remove(name);
        if(byteCode == null) {
            throw new ClassNotFoundException("not found class: " + name);
        }
        return this.defineClass(name, byteCode, 0, byteCode.length);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> foundClass = this.findLoadedClass(name);
        if(foundClass == null) {
            ClassLoader parent = this.getParent();
            if((parent instanceof ByteCodeLoader) || !name.startsWith(this.allowedPackageName)) {
                try {
                    foundClass = parent.loadClass(name);
                } catch(ClassNotFoundException ignored) {
                }
            }
        }
        if(foundClass == null) {
            foundClass = this.findClass(name);
        }
        if(resolve) {
            this.resolveClass(foundClass);
        }
        return foundClass;
    }

    /**
     * set byte code and class name.
     * before class loading, must call it.
     * @param className
     * - must be fully qualified class name.
     * @param byteCode
     */
    public void addByteCode(String className, byte[] byteCode) {
        String binaryName = toBinaryName(className);
        if(this.byteCodeMap.put(binaryName, byteCode) != null) {
            throw new FatalError("already defined class: " + className);
        }
        dump(binaryName, byteCode);
    }

    /**
     *
     * @param className
     * - must be fully qualified class name.
     * @param byteCode
     * @return
     * - if class loading failed, call System.exit(1).
     */
    public Class<?> definedAndLoadClass(String className, byte[] byteCode) {
        String binaryName = toBinaryName(className);
        this.addByteCode(binaryName, byteCode);
        try {
            return this.loadClass(binaryName);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new FatalError("class loading failed: " + binaryName);
        }
    }

    /**
     * create child class loader.
     * @return
     */
    public ByteCodeLoader createChild() {
        return new ByteCodeLoader(this);
    }

    /**
     * for debug purpose.
     */
    private static void dump(String binaryClassName, byte[] byteCode) {
        if(!Config.dumpByteCode) {
            return;
        }
        int index = binaryClassName.lastIndexOf('.');
        String classFileName = binaryClassName.substring(index + 1) + ".class";
        System.err.println("@@@@ Dump ByteCode: " + classFileName + " @@@@");
        try(FileOutputStream stream = new FileOutputStream(classFileName)) {
            stream.write(byteCode);
            stream.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static String toBinaryName(String className) {
        return className.replace('/', '.');
    }
}