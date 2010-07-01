/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
 */

package org.openadaptor.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IComponent;
import org.openadaptor.core.adaptor.Adaptor;
import org.openadaptor.core.jmx.Administrable;
import org.openadaptor.core.router.Router;
import org.openadaptor.util.Application;
import org.openadaptor.util.ResourceUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.FieldRetrievingFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;

import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Adaptor entry point for Spring Based adaptors.
 * 
 * @author OA3 Core Team
 *
 */
public class SpringApplication {

	private static Log log = LogFactory.getLog(SpringApplication.class);

	protected static final String NOPROPS = "-noprops";
	public static final String CONFIG = "-config";
	protected static final String BEAN = "-bean";
	protected static final String JMXPORT = "-jmxport";
	protected static final String PROPS = "-props";

	public static final String DEFAULT_ADAPTOR_ID="Adaptor";
	public static final String DEFAULT_ROUTER_ID="Router";
	public static final String DEFAULT_EXCEPTION_PROCESSOR_ID="ExceptionProcessor";

	//Config that SpringApplication always looks for.
	//Not sure we need it any more, but it's kept for backwards compatibility.
	protected static final String OPENADAPTOR_SPRING_CONFIG=".openadaptor-spring.xml";


	private ArrayList configUrls = new ArrayList();

	private String beanId;

	private int jmxPort;

	/** this is populated with all -props defined arguments */
	private ArrayList propsUrls = new ArrayList();

	/** If this is true we will not add a generated PropertyPlaceholderConfigurer. */
	private boolean suppressAutomaticPropsConfig = false; // default is false

	private Adaptor adaptor;

	public static void main(String[] args) {
		int exitCode=0;
		try {
			SpringApplication app = new SpringApplication();
			app.parseArgs(args);
			exitCode=app.run();   
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			usage(System.err);
			exitCode=1;
		}
		log.debug("Exiting with code "+exitCode);
		System.exit(exitCode);
	}  


	protected String getBeanId() {
		return beanId;
	}

	public void setBeanId(final String beanId) {
		this.beanId = beanId;
	}

	protected ArrayList getConfigUrls() {
		return configUrls;
	}

	public void setConfigUrls(final List configUrls) {
		this.configUrls.clear();
		this.configUrls.addAll(configUrls);
	}

	public void addConfigUrl(String configUrl) {
		this.configUrls.add(configUrl);
	}

	protected int getJmxPort() {
		return jmxPort;
	}

	public void setJmxPort(final int jmxPort) {
		this.jmxPort = jmxPort;
	}

	protected ArrayList getPropsUrls() {
		return propsUrls;
	}

	protected void setPropsUrls(ArrayList propsUrls) {
		this.propsUrls = propsUrls;
	}

	protected boolean isSuppressAutomaticPropsConfig() {
		return suppressAutomaticPropsConfig;
	}

	protected void setSuppressAutomaticPropsConfig(boolean suppressAutomaticPropsConfig) {
		this.suppressAutomaticPropsConfig = suppressAutomaticPropsConfig;
	}

	protected void parseArgs(String[] args) {
		// First check to see if automatic property configuration is suppressed
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(NOPROPS)) {
				suppressAutomaticPropsConfig = true;
			}
		}
		// Now deal with the rest of the arguments
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(CONFIG)) {
				configUrls.add(getOptionValue(args, i++));
			} else if (args[i].equals(BEAN)) {
				beanId = getOptionValue(args, i++);
			} else if (args[i].equals(JMXPORT)) {
				String jmxPortString = getOptionValue(args, i++);
				try {
					jmxPort = Integer.parseInt(jmxPortString);
					if ((jmxPort <= 0) || (jmxPort > 65535)) {
						throw new RuntimeException("Illegal jmx port specified: " + jmxPort + ". Valid range is [1-65535]");
					}
				} catch (NumberFormatException nfe) {
					throw new RuntimeException("-jmx option requires a integer port number");
				}
			} else if (args[i].equals(PROPS)) {
				propsUrls.add(getOptionValue(args, i++));
			} else if (args[i].equals(NOPROPS)) {
				// ignore because this has already been dealt with separately
			}
			else {
				throw new RuntimeException("unrecognised cmd line arg " + args[i]);
			}
		}
	}

	protected String getConfigUrlsString() {
		StringBuffer buffer = new StringBuffer();
		for (Iterator iter = configUrls.iterator(); iter.hasNext();) {
			buffer.append(buffer.length() > 0 ? "," : "").append(iter.next());
		}
		return buffer.toString();
	}

	public int run() {
		int exitCode=0;
		Runnable bean = getRunnableBean(createBeanFactory());
		if (bean instanceof Application) {
			((Application)bean).setConfigData(getConfigUrlsString());
		}
		if(bean instanceof Adaptor && adaptor==null){
			adaptor = (Adaptor) bean;
		}
		Thread.currentThread().setName(beanId);
		bean.run();
		if (adaptor!=null) {
			exitCode=adaptor.getExitCode();
		}
		return exitCode;
	}

	/**
	 * Returns the {@link Adaptor} bean from the Spring context.
	 * 
	 * @see org.openadaptor.spring.SpringApplication#getRunnableBean(org.springframework.beans.factory.ListableBeanFactory)
	 */

	protected Runnable getRunnableBean(ListableBeanFactory factory) {
		String beanId=getBeanId();
		if (beanId == null) {
			String[] ids = factory.getBeanNamesForType(Adaptor.class);
			if (ids.length == 1) {
				beanId=ids[0];
				setBeanId(beanId);
			} 
			else if (ids.length == 0){
				beanId=registerAdaptor(factory,DEFAULT_ADAPTOR_ID); //Should be set by now.
			} 
			else if (ids.length > 1) {
				throw new RuntimeException("Mulitple Adaptor beans found in config");
			}
		}
		return (Runnable) factory.getBean(beanId);
	}


	protected static void usage(PrintStream ps) {
		ps.println("usage: java " + SpringApplication.class.getName()
				+ "\n  "+CONFIG+" <url> [ "+CONFIG+" <url> ]"
				+ "\n  "+BEAN+" <id> "
				+ "\n  [ "+PROPS+" <url> [ "+PROPS+" <url> ] ]"
				+ "\n  [ "+NOPROPS+" ]"
				+ "\n  [ "+JMXPORT+" <http port>]"
				+ "\n\n"
				+ " e.g. java " + SpringApplication.class.getName() + " "+CONFIG+" file:test.xml "+BEAN+" Application");
	}

	private ListableBeanFactory createBeanFactory() {
		if (configUrls.isEmpty()) {
			throw new RuntimeException("no config urls specified");
		}
		GenericApplicationContext context = new GenericApplicationContext();
		//Changed to make failure to load OPENADAPTOR_SPRING_CONFIG non-fatal.
		try {
			loadBeanDefinitions("classpath:" + ResourceUtil.getResourcePath(this, "",OPENADAPTOR_SPRING_CONFIG), context);
		}
		catch (BeanDefinitionStoreException bdse) {
			Throwable cause=bdse.getCause();
			log.warn("Resource "+OPENADAPTOR_SPRING_CONFIG+" was not loaded. Reason: "+cause.getClass().getName());
		}
		for (Iterator iter = configUrls.iterator(); iter.hasNext();) {
			String configUrl = (String) iter.next();
			loadBeanDefinitions(configUrl, context);
		}

		configureProperties(context, propsUrls);

		context.refresh();
		setComponentIds(context);
		configureMBeanServer(context);
		return context;
	}

	/**
	 * Configure the default PropertyPlaceholderConfigurer.
	 * <br>
	 * Unless suppressAutomaticPropsConfig is set true by supplying -noprops as a command line arg then
	 * this method creates and installs a PropertyPlaceholderConfigurer which allows access to System Properties
	 * and optionally add all -props defined urls as property resource locations.<br>
	 * With one exception all urls are passed through to the context in order to locate the resource. The exception
	 * is a url that is not prefixed with a protocol is assumed to be a file and arbitratily prefixed with "file:"<br>
	 * NB the arbitrary name given to the generated PropertyPlaceholderConfigurer is "openadaptorAutoGeneratedSystemPropertyConfigurer".
	 *
	 * @param context Spring Context being used.
	 * @param propsUrlList Supplied List or Property URLS.
	 */
	protected void configureProperties(GenericApplicationContext context, ArrayList propsUrlList) {
		// System properties

		if (!suppressAutomaticPropsConfig) {

			if (context.getBeansOfType(PropertyPlaceholderConfigurer.class).size() > 0) {
				log.warn("Spring configuration file already has PropertyPlaceholderConfigurers defined. Please ensure any conflicts are resolved satisfactorily.");
			}

			MutablePropertyValues systemPropertiesModeProperties = new MutablePropertyValues();
			systemPropertiesModeProperties.addPropertyValue("staticField", "org.springframework.beans.factory.config.PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_FALLBACK");

			RootBeanDefinition systemPropertiesMode = new RootBeanDefinition(FieldRetrievingFactoryBean.class);
			systemPropertiesMode.setPropertyValues(systemPropertiesModeProperties);

			MutablePropertyValues properties = new MutablePropertyValues();
			properties.addPropertyValue("ignoreResourceNotFound", "false"); // Will cause an eror if a resource url is bogus
			Iterator propsUrlIter = propsUrlList.iterator();
			ArrayList resourceList = new ArrayList();
			while (propsUrlIter.hasNext()) {
				resourceList.add(context.getResource(ensureProtocol((String) propsUrlIter.next())));
			}
			properties.addPropertyValue("locations", resourceList);
			properties.addPropertyValue("systemPropertiesMode", systemPropertiesMode);
			RootBeanDefinition propertyHolder = new RootBeanDefinition(PropertyPlaceholderConfigurer.class);
			propertyHolder.setPropertyValues(properties);

			context.registerBeanDefinition("openadaptorAutoGeneratedSystemPropertyConfigurer", propertyHolder);
		}
	}

	private String ensureProtocol(String url) {
		String protocol = "";
		if (url.indexOf(':') != -1) {
			protocol = url.substring(0, url.indexOf(':'));
		}
		if (protocol.equals("")) { // No protocol defined try file:
			return "file:" + url;
		}
		else return url; // Lets hope its a valid protocol!.
	}

	protected void loadBeanDefinitions(String url, GenericApplicationContext context) {
		String protocol = "";
		if (url.indexOf(':') != -1) {
			protocol = url.substring(0, url.indexOf(':'));
		}

		if (protocol.equals("file") || protocol.equals("http")) {
			loadBeanDefinitionsFromUrl(url, context);
		} else if (protocol.equals("classpath")) {
			loadBeanDefinitionsFromClasspath(url, context);
		} else {
			loadBeanDefinitions("file:" + url, context);
		}
	}

	private void loadBeanDefinitionsFromClasspath(String url, GenericApplicationContext context) {
		String resourceName = url.substring(url.indexOf(':') + 1);
		BeanDefinitionReader reader = null;
		if (url.endsWith(".xml")) {
			reader = new XmlBeanDefinitionReader(context);
		} else if (url.endsWith(".properties")) {
			reader = new PropertiesBeanDefinitionReader(context);
		}

		if (reader != null) {
			reader.loadBeanDefinitions(new ClassPathResource(resourceName));
		} else {
			throw new RuntimeException("No BeanDefinitionReader associated with " + url);
		}
	}

	private void loadBeanDefinitionsFromUrl(String url, GenericApplicationContext context) {
		BeanDefinitionReader reader = null;
		if (url.endsWith(".xml")) {
			reader = new XmlBeanDefinitionReader(context);
		} else if (url.endsWith(".properties")) {
			reader = new PropertiesBeanDefinitionReader(context);
		}

		if (reader != null) {
			try {
				reader.loadBeanDefinitions(new UrlResource(url));
			} catch (BeansException e) {
				log.error("error", e);
				throw new RuntimeException("BeansException : " + e.getMessage());
			} catch (MalformedURLException e) {
				log.error("error", e);
				throw new RuntimeException("MalformedUrlException : " + e.getMessage());
			}
		} else {
			throw new RuntimeException("No BeanDefinitionReader associated with " + url);
		}
	}

	private static void setComponentIds(ListableBeanFactory factory) {
		String[] beanNames = factory.getBeanNamesForType(IComponent.class);
		for (int i = 0; i < beanNames.length; i++) {      
			Object bean = factory.getBean(beanNames[i]);
			if (bean instanceof IComponent) {
				IComponent component = (IComponent) bean;
				if (component.getId() == null) {
					component.setId(beanNames[i]);
					log.debug("setting IComponent id for " + beanNames[i]);
				} else {
					log.debug("IComponent id is already set for " + beanNames[i]);
				}
			} else {
				log.debug("bean " + beanNames[i] + " is not an IComponent");
			}
		}
	}

	private void configureMBeanServer(ListableBeanFactory factory) {
		MBeanServer mbeanServer = (MBeanServer) getFirstBeanOfType(factory, MBeanServer.class);
		if (mbeanServer == null && jmxPort != 0) {
			mbeanServer = new org.openadaptor.core.jmx.MBeanServer(jmxPort);
		}

		/* 
		 * Accesses message processors from the adaptor. Message processors that 
		 * under the hood wrap IRead & IWriteConnectors, IData & IEnrichmentProcessors
		 * are capable of exposing component level metrics. Here they're registered
		 * with the JMX server. 
		 */
		Object adaptorObj = getFirstBeanOfType(factory, Adaptor.class);
		Map messageProcessorsByName = new HashMap();
		if(adaptorObj != null){
			Collection messageProcessors = ((Adaptor) adaptorObj).getMessageProcessors();
			Iterator it = messageProcessors.iterator();
			while(it.hasNext()){
				IComponent messageProcessor = (IComponent) it.next();
				messageProcessorsByName.put(messageProcessor.getId(), messageProcessor);
			}
		}

		/* Registers components with JMX server */
		if (mbeanServer != null) {
			attemptToRegisterBean(new FactoryConfig(configUrls), mbeanServer, "Config");
			String[] beanNames = factory.getBeanDefinitionNames();
			for (int i = 0; i < beanNames.length; i++) {

				/* Register the bean */
				Object bean = factory.getBean(beanNames[i]);
				if (bean instanceof Administrable) {
					bean = ((Administrable) bean).getAdmin();
				}
				attemptToRegisterBean(bean, mbeanServer, beanNames[i]);

				/* Register the message processor corresponding to the bean */
				IComponent msgProcessor = (IComponent) messageProcessorsByName.get(beanNames[i]);
				if(msgProcessor != null && msgProcessor instanceof Administrable){
					bean = ((Administrable) msgProcessor).getAdmin();
					attemptToRegisterBean(bean, mbeanServer, beanNames[i] + "-metrics");
				}
			}
		}
	}

	private static void attemptToRegisterBean(Object bean, MBeanServer mbeanServer, String beanName) {
		if (!(bean instanceof MBeanServer)) {
			try {
				ObjectName name = new ObjectName("openadaptor:id=" + beanName);
				mbeanServer.registerMBean(bean, name);
				log.info("registered bean " + beanName);
			} catch (NotCompliantMBeanException e) {
				log.debug("bean " + beanName + " is not compliant : " + e.getMessage());
			} catch (Exception e) {
				log.error("failed to register mbean " + beanName, e);
			}
		}
	}

	private static Object getFirstBeanOfType(ListableBeanFactory factory, Class beanClass) {
		Map beanMap = factory.getBeansOfType(beanClass);
		for (Iterator iter = beanMap.values().iterator(); iter.hasNext();) {
			return iter.next();
		}
		return null;
	}

	private static String getOptionValue(String[] args, int index) {
		if (args.length > index + 1) {
			return args[index + 1];
		} else {
			throw new RuntimeException("Option " + args[index] + " requires a value, which has not been supplied");
		}
	}

	/** 
	 * @return the instance of Adaptor related to this application, for starting/stopping/dumping 
	 *         state via JMX console 
	 */
	public Adaptor getAdaptor() {
		return adaptor;
	}

	//Methods promoted from SpringAdaptor (part of the merge process for these classes).

	/**
	 * Register an {@link Adaptor} within the Spring context, using a default id.
	 * This is a convenience wrapper around registerAdaptor(factory,id);
	 * @deprecated - the id should really be supplied.
	 */
	protected String registerAdaptor(ListableBeanFactory factory) {
		return registerAdaptor(factory,DEFAULT_ADAPTOR_ID);
	}

	/**
	 * Registers an instance of an {@link Adaptor} with the Spring context.
	 * <p>
	 * Common OpenAdaptor configurations include an {@link Adaptor} with a 
	 * {@link Router} which defines an ordered list of {@link IComponent}s. If 
	 * no adaptor bean is explicitly registered, this method will auto-configure 
	 * and register one.
	 * <p>
	 * If a {@link Router} is explicitly registered in the Spring context, that
	 * will be autowired as the {@link Adaptor}'s <code>messageProcessor</code>.
	 * Otherwise, a {@link Router} will be automatically configured and registered.
	 * 
	 * @param factory the ListableBeanFactory Spring context factory
	 * @param id - String containing the id to use for the adaptor
	 * @return String containing the id for the Adaptor (
	 */

	protected String registerAdaptor(ListableBeanFactory factory,String id) {
		String[] ids = factory.getBeanNamesForType(Router.class);
		Router router = null;

		if (ids.length == 1) {
			router = (Router) factory.getBean(ids[0]);
		} 
		else if (ids.length == 0) {
			router = registerRouter(factory);
		}

		MutablePropertyValues properties = new MutablePropertyValues();
		properties.addPropertyValue("messageProcessor", router);
		RootBeanDefinition beanDefinition = new RootBeanDefinition(Adaptor.class);
		beanDefinition.setPropertyValues(properties);
		((GenericApplicationContext) factory).registerBeanDefinition(id, beanDefinition);
		setBeanId(id);
		return id;
	}

	/**
	 * Registers an instance of a {@link Router} with the Spring context. 
	 * <p>
	 * This method adds all beans registered in the Spring context that are
	 * instances of {@link IComponent} to the router's <code>processors</code> 
	 * list in <strong>the order in which they are declared in the Spring 
	 * configuration</strong>.
	 * <p>
	 * The only exception is any bean with an id of <code>ExceptionProcessor</code>,
	 * which is set as the <code>exceptionProcessor</code> property of the
	 * {@link Router}.
	 * <p>
	 * Note that this means only basic, single pipeline router configurations
	 * may be auto-configured in this way. Complex configurations that require
	 * a <code>processMap</code> must be defined explicitly. 
	 * 
	 * @param factory the ListableBeanFactory Spring context factory
	 * @return a configured Router
	 */
	protected Router registerRouter(ListableBeanFactory factory) {
		String[] ids = factory.getBeanNamesForType(IComponent.class);

		if (ids.length == 0) {
			throw new RuntimeException("No Component beans found in config");
		}

		List processors = new ArrayList(ids.length);
		Object exceptionProcessor = null;

		for (int i = 0; i < ids.length; i++) {
			if (!ids[i].equals(DEFAULT_EXCEPTION_PROCESSOR_ID)) {
				processors.add(factory.getBean(ids[i]));
			} else {
				exceptionProcessor = factory.getBean(ids[i]);
			}
		}

		MutablePropertyValues properties = new MutablePropertyValues();
		properties.addPropertyValue("processors", processors);

		if (exceptionProcessor != null) {
			properties.addPropertyValue("exceptionProcessor", exceptionProcessor);
		}

		RootBeanDefinition beanDefinition = new RootBeanDefinition(Router.class);
		beanDefinition.setPropertyValues(properties);
		((GenericApplicationContext) factory).registerBeanDefinition(DEFAULT_ROUTER_ID, beanDefinition);

		return (Router) factory.getBean(DEFAULT_ROUTER_ID);
	}
}
