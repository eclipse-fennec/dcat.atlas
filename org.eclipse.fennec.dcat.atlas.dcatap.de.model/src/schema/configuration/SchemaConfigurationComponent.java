/*
 */
package schema.configuration;

import java.util.Hashtable;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.resource.Resource.Factory;

import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;

import org.osgi.annotation.bundle.Capability;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import org.osgi.service.condition.Condition;

import schema.SchemaFactory;
import schema.SchemaPackage;

import schema.impl.SchemaPackageImpl;

import schema.util.SchemaResourceFactoryImpl;
/**
 * The <b>PackageConfiguration</b> for the model.
 * The package will be registered into a OSGi base model registry.
 * 
 * @generated
 */
@Component(name = "SchemaConfigurator")
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"schema.util.SchemaResourceFactoryImpl, org.eclipse.emf.ecore.resource.Resource$Factory\"" , "uses:=\"org.eclipse.emf.ecore.resource,schema.util\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"schema.SchemaFactory, org.eclipse.emf.ecore.EFactory\"" , "uses:=\"org.eclipse.emf.ecore,schema\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"schema.SchemaPackage, org.eclipse.emf.ecore.EPackage\"" , "uses:=\"org.eclipse.emf.ecore,schema\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator\"" , "uses:=\"org.eclipse.emf.ecore,schema\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"org.osgi.service.condition.Condition\"" , "uses:=org.osgi.service.condition" })
public class SchemaConfigurationComponent {
	
	private ServiceRegistration<?> packageRegistration = null;
	private ServiceRegistration<EPackageConfigurator> ePackageConfiguratorRegistration = null;
	private ServiceRegistration<?> eFactoryRegistration = null;
	private ServiceRegistration<?> conditionRegistration = null;
	private ServiceRegistration<?> resourceFactoryRegistration = null;

	/**
	 * Activates the Configuration Component.
	 *
	 * @generated
	 */
	@Activate
	public void activate(BundleContext ctx) {
	
		checkEMFEcore(ctx);
		SchemaPackage ePackage = SchemaPackageImpl.eINSTANCE;
		
		if(!EPackage.Registry.INSTANCE.containsKey(SchemaPackage.eNS_URI)){
			EPackage.Registry.INSTANCE.put(SchemaPackage.eNS_URI, ePackage);
		}
		
		SchemaEPackageConfigurator packageConfigurator = registerEPackageConfiguratorService(ePackage, ctx);
		registerResourceFactoryService(ctx);
		registerEPackageService(ePackage, packageConfigurator, ctx);
		registerEFactoryService(ePackage, packageConfigurator, ctx);
		registerConditionService(packageConfigurator, ctx);
	}
	
	/**
	 * We have to make sure that org.eclipse.emf.ecore is started, so we don't run 
	 * into start order issues due to the use of static access in EMF 
	 * @param ctx the {@link BundleContext} to use
	 */
	private void checkEMFEcore(BundleContext ctx) {
		Bundle[] bundles = ctx.getBundles();
		
		for(Bundle bundle : bundles) {
			if("org.eclipse.emf.ecore".equals(bundle.getSymbolicName())) {
				try {
					bundle.start();
				} catch (BundleException e) {
					System.err.println("Could not start Bundle org.eclipse.emf.ecore, something seems seriously wrong: " + e.getMessage());
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	/**
	 * Registers the SchemaEPackageConfigurator as a service.
	 *
	 * @generated
	 */
	private SchemaEPackageConfigurator registerEPackageConfiguratorService(SchemaPackage ePackage, BundleContext ctx){
		SchemaEPackageConfigurator packageConfigurator = new SchemaEPackageConfigurator(ePackage);
		// register the EPackageConfigurator
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		ePackageConfiguratorRegistration = ctx.registerService(EPackageConfigurator.class, packageConfigurator, properties);

		return packageConfigurator;
	}

	/**
	 * Registers the SchemaResourceFactoryImpl as a service.
	 *
	 * @generated
	 */
	private void registerResourceFactoryService(BundleContext ctx){
		SchemaResourceFactoryImpl factory = new SchemaResourceFactoryImpl();
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(factory.getServiceProperties());
		String[] serviceClasses = new String[] {SchemaResourceFactoryImpl.class.getName(), Factory.class.getName()};
		resourceFactoryRegistration = ctx.registerService(serviceClasses, factory, properties);
	}

	/**
	 * Registers the SchemaPackage as a service.
	 *
	 * @generated
	 */
	private void registerEPackageService(SchemaPackage ePackage, SchemaEPackageConfigurator packageConfigurator, BundleContext ctx){
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		String[] serviceClasses = new String[] {SchemaPackage.class.getName(), EPackage.class.getName()};
		packageRegistration = ctx.registerService(serviceClasses, ePackage, properties);
	}

	/**
	 * Registers the SchemaFactory as a service.
	 *
	 * @generated
	 */
	private void registerEFactoryService(SchemaPackage ePackage, SchemaEPackageConfigurator packageConfigurator, BundleContext ctx){
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		String[] serviceClasses = new String[] {SchemaFactory.class.getName(), EFactory.class.getName()};
		eFactoryRegistration = ctx.registerService(serviceClasses, ePackage.getSchemaFactory(), properties);
	}

	private void registerConditionService(SchemaEPackageConfigurator packageConfigurator, BundleContext ctx){
		// register the EPackage
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		properties.put(Condition.CONDITION_ID, SchemaPackage.eNS_URI);
		conditionRegistration = ctx.registerService(Condition.class, Condition.INSTANCE, properties);
	}

	/**
	 * Deactivates and unregisters everything.
	 *
	 * @generated
	 */
	@Deactivate
	public void deactivate() {
		conditionRegistration.unregister();
		eFactoryRegistration.unregister();
		packageRegistration.unregister();
		resourceFactoryRegistration.unregister();

		ePackageConfiguratorRegistration.unregister();
		EPackage.Registry.INSTANCE.remove(SchemaPackage.eNS_URI);
	}
}
