package org.openadaptor.bootstrap;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import org.openadaptor.util.ClasspathBootstrapper;

public class SpringApplication {
  public static void main(String[] args) throws SecurityException, IllegalArgumentException, MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    ClasspathBootstrapper.main("org.openadaptor.spring.SpringApplication", args);
  }
}
