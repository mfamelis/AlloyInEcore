package eu.modelwriter.core.alloyinecore.ui.mapping.cs2as;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.resource.Resource;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreBaseVisitor;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EAttributeContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EClassContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EClassifierContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EDataTypeContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EEnumContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EOperationContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EPackageContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EReferenceContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.EStructuralFeatureContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.ETypeParameterContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.PackageImportContext;
import eu.modelwriter.core.alloyinecore.recognizer.AlloyInEcoreParser.TemplateSignatureContext;
import eu.modelwriter.core.alloyinecore.ui.mapping.AIEImport;

public class CS2ASInitializer extends AlloyInEcoreBaseVisitor<Object> {

  private final Stack<String> qualifiedNameStack;
  private final CS2ASRepository repository;

  public CS2ASInitializer(final CS2ASRepository repository) {
    qualifiedNameStack = new Stack<>();
    this.repository = repository;
  }

  public void initialize(final ParseTree tree) {
    visit(tree);
  }

  @Override
  public Object visitPackageImport(final PackageImportContext ctx) {
    String path = null;
    if (ctx.ownedPathName != null) {
      path = ctx.ownedPathName.getText().replace("'", "");
    }

    final Resource resource = repository.loadResource(path);

    final String name = ctx.name != null ? ctx.name.getText()
        : resource.getContents().get(0) instanceof ENamedElement
            ? ((ENamedElement) resource.getContents().get(0)).getName()
            : null;

    final AIEImport aieImport =
        AIEImport.newInstance().setName(name).setPath(path).setResource(resource);
    repository.name2Import.put(name, aieImport);
    return null;
  }

  boolean isRoot = true;
  private int pc = 0;

  @Override
  public EPackage visitEPackage(final EPackageContext ctx) {
    final EPackage ePackage = repository.factory.createEPackage();

    String name = "package" + ++pc;
    if (ctx.name != null) {
      name = ctx.name.getText();
    }
    ePackage.setName(name);

    if (isRoot) {
      repository.aieResource.getContents().add(ePackage);
      repository.name2Import.put(name, new AIEImport().setName(name).setPath(ctx.nsURI.getText())
          .setResource(repository.aieResource));
      isRoot = false;
    }

    qualifiedNameStack.push(name);

    ctx.eSubPackages.forEach(sp -> {
      final EPackage subPackage = visitEPackage(sp);
      ePackage.getESubpackages().add(subPackage);
    });

    ctx.eClassifiers.forEach(c -> {
      final EClassifier classifier = visitEClassifier(c);
      ePackage.getEClassifiers().add(classifier);
    });

    qualifiedNameStack.pop();

    // TODO if there is any annotation which has OwnedContent (EModelElement), it need to be
    // initialized with super.visitEPackage and need to has a (complex) qualified name

    return ePackage;
  }

  @Override
  public EClassifier visitEClassifier(final EClassifierContext ctx) {
    return (EClassifier) super.visitEClassifier(ctx);
  }

  private int cc = 0;

  @Override
  public EClass visitEClass(final EClassContext ctx) {
    final EClass eClass = repository.factory.createEClass();

    String name = "class" + ++cc;
    if (ctx.name != null) {
      name = ctx.name.getText();
    }
    eClass.setName(name);

    qualifiedNameStack.push(name);

    if (ctx.ownedSignature != null) {
      final List<ETypeParameter> eTypeParameters = visitTemplateSignature(ctx.ownedSignature);
      eClass.getETypeParameters().addAll(eTypeParameters);
    }

    ctx.eStructuralFeatures.forEach(esf -> {
      final EStructuralFeature eStructuralFeature = visitEStructuralFeature(esf);
      if (eStructuralFeature != null)
        eClass.getEStructuralFeatures().add(eStructuralFeature);
      // TODO is it required to add eStructuralFeature to eClass.getEReferences() or to
      // eClass.getEAttributes() ? Discuss.
    });

    ctx.eOperations.forEach(eo -> {
      final EOperation eOperation = visitEOperation(eo);
      eClass.getEOperations().add(eOperation);
    });

    qualifiedNameStack.pop();

    // TODO if there is any annotation which has OwnedContent (EModelElement), it need to be
    // initialized with super.visitEClass and need to has a (complex) qualified name

    return eClass;
  }

  private int dtc = 0;

  @Override
  public EDataType visitEDataType(final EDataTypeContext ctx) {
    final EDataType eDataType = repository.factory.createEDataType();

    String name = "datatype" + ++dtc;
    if (ctx.name != null) {
      name = ctx.name.getText();
    }
    eDataType.setName(name);

    if (ctx.ownedSignature != null) {
      final List<ETypeParameter> eTypeParameters = visitTemplateSignature(ctx.ownedSignature);
      eDataType.getETypeParameters().addAll(eTypeParameters);
    }

    // qualifiedNameStack.push(name);
    // TODO if there is any annotation which has OwnedContent (EModelElement), it need to be
    // initialized with super.visitEDataType and need to has a (complex) qualified name
    // qualifiedNameStack.pop();

    return eDataType;
  }

  private int ec = 0;

  @Override
  public EEnum visitEEnum(final EEnumContext ctx) {
    final EEnum eEnum = repository.factory.createEEnum();

    String name = "enum" + ++ec;
    if (ctx.name != null) {
      name = ctx.name.getText();
    }
    eEnum.setName(name);

    if (ctx.ownedSignature != null) {
      final List<ETypeParameter> eTypeParameters = visitTemplateSignature(ctx.ownedSignature);
      eEnum.getETypeParameters().addAll(eTypeParameters);
    }

    // qualifiedNameStack.push(name);
    // TODO if there is any annotation which has OwnedContent (EModelElement), it need to be
    // initialized with super.visitEEnum and need to has a (complex) qualified name
    // qualifiedNameStack.pop();

    return eEnum;
  }

  @Override
  public EStructuralFeature visitEStructuralFeature(final EStructuralFeatureContext ctx) {
    return (EStructuralFeature) super.visitEStructuralFeature(ctx);
  }

  private int rc = 0;

  @Override
  public EReference visitEReference(final EReferenceContext ctx) {

    final EReference eReference = repository.factory.createEReference();

    String name = "reference" + ++rc;
    if (ctx.name != null) {
      name = ctx.name.getText();
    }
    eReference.setName(name);

    // qualifiedNameStack.push(name);
    // TODO if there is any annotation which has OwnedContent (EModelElement), it need to be
    // initialized with super.visitEReference and need to has a (complex) qualified name
    // qualifiedNameStack.pop();

    return eReference;
  }

  private int ac = 0;

  @Override
  public EAttribute visitEAttribute(final EAttributeContext ctx) {

    final EAttribute eAttribute = repository.factory.createEAttribute();

    String name = "attribute" + ++ac;
    if (ctx.name != null) {
      name = ctx.name.getText();
    }
    eAttribute.setName(name);

    // qualifiedNameStack.push(name);
    // TODO if there is any annotation which has OwnedContent (EModelElement), it need to be
    // initialized with super.visitEAttribute and need to has a (complex) qualified name
    // qualifiedNameStack.pop();

    return eAttribute;
  }

  private int oc = 0;

  @Override
  public EOperation visitEOperation(final EOperationContext ctx) {
    final EOperation eOperation = repository.factory.createEOperation();

    String name = "operation" + ++oc;
    if (ctx.name != null) {
      name = ctx.name.getText();
    }
    eOperation.setName(name);

    if (ctx.ownedSignature != null) {
      final List<ETypeParameter> eTypeParameters = visitTemplateSignature(ctx.ownedSignature);
      eOperation.getETypeParameters().addAll(eTypeParameters);
    }

    // qualifiedNameStack.push(name);
    // TODO if there is any annotation which has OwnedContent (EModelElement), it need to be
    // initialized with super.visitEOperation and need to has a (complex) qualified name
    // qualifiedNameStack.pop();

    return eOperation;
  }

  @Override
  public List<ETypeParameter> visitTemplateSignature(final TemplateSignatureContext ctx) {
    return ctx.ownedTypeParameters.stream().map(op -> visitETypeParameter(op))
        .collect(Collectors.toList());
  }

  private int tpc = 0;

  @Override
  public ETypeParameter visitETypeParameter(final ETypeParameterContext ctx) {
    final ETypeParameter eTypeParameter = repository.factory.createETypeParameter();

    String name = "typeparameter" + ++tpc;
    if (ctx.name != null) {
      name = ctx.name.getText();
    }
    eTypeParameter.setName(name);

    return eTypeParameter;
  }
}
