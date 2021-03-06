grammar Imports;
//https://github.com/antlr/antlr4/blob/master/doc/wildcard.md

@parser::header {
import eu.modelwriter.core.alloyinecore.structure.base.Element;
import eu.modelwriter.core.alloyinecore.structure.imports.ImportedPackage;
import eu.modelwriter.core.alloyinecore.structure.imports.ImportedClass;
import eu.modelwriter.core.alloyinecore.structure.imports.ImportedInterface;
import eu.modelwriter.core.alloyinecore.structure.imports.ImportedDataType;
import eu.modelwriter.core.alloyinecore.structure.imports.ImportedEnum;
import eu.modelwriter.core.alloyinecore.structure.model.Import;
}

@parser::members {
private Element owningElement;
}

importModel[Element owner]
@init{owningElement=owner;}:
    .*? iPackage[owningElement]*
    ;

iPackage[Element owner] locals[ImportedPackage current]
@after{if(owner != null) owner.addOwnedElement($current); else owningElement= $current;}:
    visibility? 'package' name= unrestrictedName {$current = new ImportedPackage($ctx);}
    ':' nsPrefix= identifier '=' nsURI= SINGLE_QUOTED_STRING  ('{' (iClassifier[$current] | iPackage[$current] | . )*? '}' | ';')

    ;

iClassifier[Element owner]:
    (iClass[$owner] | iDataType[$owner]) (block | ';');

iClass[Element owner] locals[ImportedClass current]
@after{owner.addOwnedElement($current);}:
    visibility? (isAbstract= 'abstract'? isClass='class' | isInterface= 'interface') name= unrestrictedName templateSignature? ('extends' iGenericType (',' iGenericType)*)?
    {if ($isInterface!=null) $current = new ImportedInterface($ctx); else $current = new ImportedClass($ctx);}
    ;

iDataType[Element owner] locals[ImportedDataType current]
@after{owner.addOwnedElement($current);}:
    visibility? (isDataType= 'datatype' | isEnum= 'enum') name= unrestrictedName templateSignature? (':' instanceClassName= SINGLE_QUOTED_STRING)?
    {if ($isDataType!=null) $current = new ImportedDataType($ctx); else $current = new ImportedEnum($ctx);}
    ;

visibility: 'public' | 'protected'| 'private' ;

block : '{' (block | .)*? '}' ;

templateSignature: '<'  iTypeParameter (',' iTypeParameter)* '>';

iTypeParameter: unrestrictedName ('extends' iGenericType ('&' iGenericType)* )?;

iGenericType: pathName ('<' iGenericTypeArgument (',' iGenericTypeArgument)* '>')? ;

iGenericTypeArgument: iGenericType | iGenericWildcard ;

iGenericWildcard: '?' (bound=('extends' | 'super') iGenericType)? ;

pathName:firstSegment= unrestrictedName ('.' index= INT)? (midSegments+= segment*  lastSegment= segment)? ;

segment: '::' '@'? name= unrestrictedName ('.' index= INT)? ;

unrestrictedName:
        identifier
    |	'abstract'
    |	'attribute'
    |	'body'
    |	'callable'
    |	'class'
    |	'composes'
    |	'datatype'
    |	'definition'
    |	'derivation'
    |	'derived'
    |	'enum'
    |   'ensure'
    |	'extends'
    |	'id'
    |	'import'
    |	'initial'
    |	'interface'
    |	'key'
    |	'model'
    |	'operation'
    |	'ordered'
    |	'package'
    |	'postcondition'
    |	'precondition'
    |	'primitive'
    |	'property'
    |	'readonly'
    |	'reference'
    |   'require'
    |	'resolve'
    |	'static'
    |	'throws'
    |	'transient'
    |	'unique'
    |	'unsettable'
    |	'volatile'
    |	'invariant'
    |	'literal'
    |	'serializable'
    |	'annotation'
    |	'model'

;

identifier: IDENTIFIER;
//upper: INT | '*';
//lower: INT;

INT :   DIGIT+ ;
IDENTIFIER : (UNDERSCORE | LETTER) (LETTER | APOSTROPHE | DIGIT | UNDERSCORE | DOLLAR)* ;
SINGLE_CHARACTER: '\'' ~['\\] '\'';
DOUBLE_QUOTED_STRING: '"' ( ESCAPED_CHARACTER | ~('\\' | '"' ) )* '"'  ;
SINGLE_QUOTED_STRING: '\'' ( ESCAPED_CHARACTER | ~('\'' | '\\') )* '\'' ;

fragment LETTER: [a-zA-Z];
fragment DIGIT: [0-9];
fragment ESCAPED_CHARACTER: '\\' ('b' | 't' | 'n' | 'f' | 'r' | 'u' | '"' | '\'' | '\\');
fragment UNDERSCORE: '_';
fragment APOSTROPHE: '\'';
fragment DOLLAR: '$';
fragment EXCLAMINATION_MARK: '!';
fragment MINUS: '-';
ML_SINGLE_QUOTED_STRING : '\'' .*? '\'' -> skip;
MULTILINE_COMMENT : '/*' .*? '*/' -> skip;
SINGLELINE_COMMENT : ('--' | '//') .*? '\r'? '\n' -> skip;
WS: [ \t\r\n]+ -> skip ; // toss out whitespace
ANY : . ;