package eu.modelwriter.core.alloyinecore.ui.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

public class EcoreUtilities {

  public static final String EOBJECT_URI = "relative_eobject_uri";
  public static final String ROOT_URI = "relative_ecore_root_uri";

  public static XMLSave.XMLTypeInfo noTypeInfo;

  static {
    EcoreUtilities.noTypeInfo = new XMLSave.XMLTypeInfo() {

      @Override
      public boolean shouldSaveType(final EClass objectType, final EClass featureType,
          final EStructuralFeature feature) {
        return false;
      }

      @Override
      public boolean shouldSaveType(final EClass objectType, final EClassifier featureType,
          final EStructuralFeature feature) {
        return false;
      }
    };
  }

  /**
   * Gets root EObject of given xmi file path
   *
   * @param xmiFileFullPath file path of xmi file
   * @return root @EObject
   * @throws IOException
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static Resource loadInstanceModel(final String xmiFileFullPath) throws IOException {
    final Map options = new HashMap();
    options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);

    final ResourceSetImpl resourceSet = new ResourceSetImpl();
    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
        .put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
    Resource resource =
        resourceSet.getResource(URI.createPlatformResourceURI(xmiFileFullPath, true), true);
    if (resource == null) {
      resource = resourceSet.getResource(URI.createFileURI(xmiFileFullPath), true);
      if (resource == null) {
        resource =
            resourceSet.getResource(URI.createPlatformPluginURI(xmiFileFullPath, true), true);
        if (resource == null) {
          resource = resourceSet.getResource(URI.createURI(xmiFileFullPath), true);
        }
      }
    }
    resource.load(options);
    if (resource.isLoaded()) {
      return resource;
    }
    return null;
  }

  /**
   *
   * @param eObject possibly the package to find all @EClass's under it
   * @return @List of @EClass names
   */
  public static List<String> getAllEClassNames(final EObject eObject) {
    final List<EClass> classes = new ArrayList<>();
    EcoreUtilities.recursiveGetEClasses(eObject, classes);
    final List<String> classNames = new ArrayList<>();
    for (final EClass eClass : classes) {
      classNames.add(eClass.getName());
    }
    return classNames;
  }

  /**
   *
   * @param eObject possibly the package to find all @EClass under it
   * @return @List of all @EClass
   */
  public static List<EClass> getAllEClass(final EObject eObject) {
    final List<EClass> classes = new ArrayList<>();
    EcoreUtilities.recursiveGetEClasses(eObject, classes);
    return classes;
  }

  private static void recursiveGetEClasses(final EObject object, final List<EClass> classes) {
    for (final EObject eObject : object.eContents()) {
      if (eObject instanceof EClass) {
        classes.add((EClass) eObject);
      } else if (eObject instanceof EPackage) {
        EcoreUtilities.recursiveGetEClasses(eObject, classes);
      }
    }
  }

  /**
   *
   * @param @EObject object to be set
   * @param name reference name
   * @param newVal new value
   */
  public static void eSetAttributeByName(final EObject eObject, final String name,
      final Object newVal) {
    for (final EAttribute eAttribute : eObject.eClass().getEAllAttributes()) {
      if (eAttribute.getName().equals(name)) {
        eObject.eSet(eAttribute, newVal);
        break;
      }
    }
  }

  /**
   *
   * @param @EObject object to be set
   * @param name reference name
   * @param newVal new value
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void eSetReferenceByName(final EObject eObject, final String name,
      final Object newVal) {
    for (final EReference eReference : eObject.eClass().getEAllReferences()) {
      if (eReference.getName().equals(name)) {
        if (eReference.isMany()) {
          ((List) eObject.eGet(eReference)).add(newVal);
        } else {
          eObject.eSet(eReference, newVal);
        }
        break;
      }
    }
  }

  /**
   *
   * @param @EObject object to be set
   * @param name structural feature name
   * @param newVal new value
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void eSetStructuralFeatureByName(final EObject eObject, final String name,
      final Object newVal) {
    for (final EStructuralFeature eStructuralFeature : eObject.eClass().getEAllStructuralFeatures()) {
      if (eStructuralFeature.getName().equals(name)) {
        if (eStructuralFeature.isMany()) {
          ((List) eObject.eGet(eStructuralFeature)).add(newVal);
        } else {
          eObject.eSet(eStructuralFeature, newVal);
        }
        break;
      }
    }
  }

  /**
   *
   * @param root @EObject
   * @param className class name to be find
   * @return
   */
  public static EClass findEClass(final EObject root, final String className) {
    final List<EClass> allEClass = EcoreUtilities.getAllEClass(root);
    return allEClass.stream().filter(c -> c.getName().equals(className)).findFirst().orElse(null);
  }

  /**
   *
   * @param container
   * @param eObject
   * @return
   */
  public static EReference getContainmentEReference(final EObject container,
      final EObject eObject) {
    for (final EReference eReference : container.eClass().getEAllReferences()) {
      if ((eObject.eClass().getName().equals(eReference.getEReferenceType().getName())
          || eObject.eClass().getEAllSuperTypes().stream()
              .anyMatch(s -> s.getName().equals(eReference.getEReferenceType().getName())))
          && eReference.isContainment()) {
        return eReference;
      }
    }
    return null;
  }

  /**
   * Puts given dynamic EObject to container
   *
   * @param container
   * @param eObject
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void putIntoContainer(final EObject container, final EObject eObject) {
    final EReference eReference = EcoreUtilities.getContainmentEReference(container, eObject);
    if (eReference != null) {
      if (eReference.isMany()) {
        ((List) container.eGet(eReference)).add(eObject);
      } else {
        container.eSet(eReference, eObject);
      }
    }
  }

  public static EClass findEClass(final List<EClass> allEClasses, final String className) {
    return allEClasses.stream().filter(c -> c.getName().equals(className)).findFirst().orElse(null);
  }

  /**
   * Gets first root EObject (index = 0) of given uri
   *
   * @param uri
   * @return root @EObject
   * @throws IOException
   */
  public static EObject getRootObject(final URI uri) throws IOException {
    final List<EModelElement> models = EcoreUtilities.loadMetaModel(uri);
    return models == null ? null : models.get(0);
  }

  /**
   * Loads EMF Metamodel via given @URI
   *
   * @param uri Metamodel's @URI
   * @return List of @EModelElement
   * @throws IOException
   */
  public static List<EModelElement> loadMetaModel(final URI uri) throws IOException {
    List<EModelElement> list = null;
    try {
      final ModelIO<EModelElement> modelIO = new ModelIO<>();
      list = modelIO.read(uri);
    } catch (final Exception e) {
      return null;
    }
    return list;
  }

  /**
   * Gets first root EObject (index = 0) of any given path
   *
   * @param anyPath
   * @return root @EObject
   * @throws IOException
   */
  public static EObject getRootObject(final String anyPath) throws IOException {
    final List<EModelElement> elems = EcoreUtilities.loadMetaModel(anyPath);
    return elems != null ? elems.get(0) : null;
  }

  /**
   * Loads EMF Metamodel via given path
   *
   * @param anyPath
   * @return root @EObject
   * @throws IOException
   */
  public static List<EModelElement> loadMetaModel(final String anyPath) throws IOException {
    List<EModelElement> elems =
        EcoreUtilities.loadMetaModel(URI.createPlatformResourceURI(anyPath, true));
    if (elems == null) {
      elems = EcoreUtilities.loadMetaModel(URI.createFileURI(anyPath));
      if (elems == null) {
        elems = EcoreUtilities.loadMetaModel(URI.createPlatformPluginURI(anyPath, true));
        if (elems == null) {
          elems = EcoreUtilities.loadMetaModel(URI.createURI(anyPath));
        }
      }
    }
    return elems;
  }

  /**
   * Saves given @EObject to its resource.
   *
   * @param root @EObject
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static void saveResource(final EObject root) {
    final Map options = new HashMap();
    options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
    // options.put(XMLResource.OPTION_SAVE_TYPE_INFORMATION, noTypeInfo);

    try {
      root.eResource().save(options);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void saveResource(final EObject root, final URI uri) {
    final List<EObject> roots = new ArrayList<>();
    roots.add(root);
    EcoreUtilities.saveResources(roots, uri);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static void saveResources(final List<EObject> root, final URI uri) {
    final ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
        .put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMLResourceFactoryImpl());
    final Resource resource = resourceSet.createResource(uri);
    resource.getContents().addAll(root);

    final Map options = new HashMap();
    options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
    try {
      resource.save(options);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
}
